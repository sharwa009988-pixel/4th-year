package com.interviewprep.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic in-memory rate limiting (per-user/per-IP).
 *
 * Notes:
 * - For production, replace with Redis-backed bucket4j or Spring Cloud Gateway rate limiting.
 * - This implementation is intentionally simple but effective for demos and local use.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000; // 1 minute window

    private static final int AUTH_LIMIT_PER_WINDOW = 15;
    private static final int AI_LIMIT_PER_WINDOW = 30;
    private static final int CODE_LIMIT_PER_WINDOW = 15;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only rate limit API endpoints (skip actuator health)
        return !(path.startsWith("/api/") || path.startsWith("/actuator/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.equals("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = resolveLimit(path);
        String key = buildKey(request, path);

        if (!allow(key, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please wait and try again.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.startsWith("/api/auth/")) return AUTH_LIMIT_PER_WINDOW;
        if (path.startsWith("/api/code/")) return CODE_LIMIT_PER_WINDOW;
        if (path.startsWith("/api/interviews/")) return AI_LIMIT_PER_WINDOW;
        return 120; // default
    }

    private String buildKey(HttpServletRequest request, String path) {
        String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        String ip = clientIp(request);
        // Separate buckets by major endpoint group
        String group = path.startsWith("/api/auth/") ? "auth"
                : path.startsWith("/api/code/") ? "code"
                : path.startsWith("/api/interviews/") ? "ai"
                : "other";
        return group + "|" + user + "|" + ip;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private boolean allow(String key, int limit) {
        long now = Instant.now().toEpochMilli();
        WindowCounter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.windowStartMs) >= WINDOW_MS) {
                return new WindowCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        boolean allowed = counter.count <= limit;
        if (!allowed) {
            log.warn("Rate limit exceeded for key={} count={}", key, counter.count);
        }
        return allowed;
    }

    private static class WindowCounter {
        final long windowStartMs;
        int count;

        WindowCounter(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}

