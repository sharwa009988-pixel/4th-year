package com.interviewprep.service;

import com.interviewprep.util.PromptTemplates;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import java.util.ArrayList;

/**
 * AI Service using Grok (xAI) via direct HTTP calls. This replaces Spring AI ChatClient usage
 * which can be misconfigured for local deployments. The implementation is resilient
 * to multiple provider response shapes and uses a short timeout to avoid blocking the UI.
 */
@Service
@Slf4j
public class AiService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;

    /** Timeout in seconds for each provider call; after this we return quickly so UI doesn't hang. */
    @Value("${app.grok.timeout-seconds:10}")
    private int grokTimeoutSeconds;

    /** Number of attempts to call Grok before giving up */
    @Value("${app.grok.max-retries:2}")
    private int grokMaxRetries;

    @Value("${spring.ai.grok.base-url:https://api.x.ai}")
    private String grokBaseUrl;
    
    public AiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateQuestion(String role,
                                   String topic,
                                   String difficulty,
                                   String questionType,
                                   String interviewType) {
        try {
            String systemPrompt = PromptTemplates.buildSystemPrompt(role);

            // Add a small random seed to encourage the LLM to vary outputs between calls
            int seed = ThreadLocalRandom.current().nextInt(1, 1_000_000);
            String userPrompt = PromptTemplates.generateQuestionPrompt(questionType, interviewType, topic, difficulty)
                + "\n\nPlease generate a different/new question than previous ones. RandomSeed: " + seed;

            List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            );

            // Add a randomized temperature to encourage diversity from the LLM provider
            double temperature = 0.7 + ThreadLocalRandom.current().nextDouble() * 0.2; // 0.7 - 0.9
            Map<String, String> options = Map.of("model", "grok-beta", "temperature", String.valueOf(temperature));
            String response = sendGrokWithRetries(messages, options);
            if (response != null && !response.isBlank() && !response.startsWith("ERROR_QUOTA_EXCEEDED:")) {
                log.debug("Generated question via Grok for role: {}, topic: {}, type: {}", role, topic, questionType);
                String text = response.trim();
                if ("MCQ".equalsIgnoreCase(questionType)) {
                    text = sanitizeMcqQuestion(text);
                }
                return text;
            } else {
                log.warn("Grok returned empty or quota exceeded for generateQuestion");
                return "";
            }
        } catch (Exception e) {
            log.warn("Grok question generation failed: {}", e.getMessage());
            return "";
        }
    }

    /**
    * Local fallback generator used when Grok is unreachable or times out.
     * Produces simple, deterministic questions so the UI remains usable.
     */
    private String generateFallbackQuestion(String role, String topic, String difficulty, String questionType, String interviewType) {
        String t = (topic == null || topic.isBlank()) ? null : topic;
        String r = (role == null || role.isBlank()) ? "the role" : role;
        String diff = (difficulty == null || difficulty.isBlank()) ? "MEDIUM" : difficulty;

        // Use a few template variations and pick one at random to avoid deterministic repetition
        int idx = ThreadLocalRandom.current().nextInt(0, 5);

        if ("MCQ".equalsIgnoreCase(questionType)) {
            List<String> mcqTemplates = new ArrayList<>();
            mcqTemplates.add("Which time complexity best describes binary search?\n\nA) O(n)\nB) O(log n)\nC) O(n log n)\nD) O(1)");
            mcqTemplates.add("Which HTTP method is idempotent by definition?\n\nA) POST\nB) PUT\nC) PATCH\nD) CONNECT");
            mcqTemplates.add("Which data structure provides FIFO ordering?\n\nA) Stack\nB) Queue\nC) Heap\nD) Set");
            mcqTemplates.add("Which SQL clause filters rows before aggregation?\n\nA) ORDER BY\nB) HAVING\nC) WHERE\nD) GROUP BY");
            mcqTemplates.add("Which Java keyword prevents subclassing?\n\nA) static\nB) final\nC) private\nD) abstract");

            return mcqTemplates.get(idx % mcqTemplates.size());
        }

        if ("CODING".equalsIgnoreCase(questionType)) {
            List<String> codingTemplates = new ArrayList<>();
            codingTemplates.add("Implement a function that merges overlapping intervals and returns a minimized list. Provide examples and discuss complexity.");
            codingTemplates.add("Write a function to compute the k most frequent elements in an array. Include tests and explain the time/space trade‑offs.");
            codingTemplates.add("Implement an LRU cache with get/put in O(1) average time. Describe the data structures used and add basic tests.");
            codingTemplates.add("Given a string, return the longest substring without repeating characters. Provide examples and analyze complexity.");
            codingTemplates.add("Implement a function to validate balanced brackets for (), {}, []. Include edge cases and unit tests.");

            return codingTemplates.get(idx % codingTemplates.size());
        }

        // Default: subjective variations
        List<String> subj = List.of(
                String.format("Design a REST API for order management: resources, endpoints, status codes, validation, error handling, and pagination. Discuss trade‑offs in %s (difficulty: %s).", r, diff),
                String.format("Explain Spring Boot transaction management using @Transactional: propagation, isolation, and common pitfalls (lazy loading, exceptions). Provide a short example relevant to %s (difficulty: %s).", r, diff),
                String.format("Model a relational schema for e‑commerce (users, orders, items, payments). Show key tables and relationships, and write two example SQL queries. Discuss indexing trade‑offs for %s (difficulty: %s).", r, diff),
                String.format("Describe an approach to secure a React + Spring Boot app: authentication flow, JWT handling, CSRF considerations, and role‑based authorization in %s (difficulty: %s).", r, diff),
                String.format("Debug a production issue: slow checkout endpoint. Outline how you would observe logs/metrics, profile code, analyze queries, and roll out a fix safely in %s (difficulty: %s).", r, diff)
        );

        return subj.get(idx % subj.size());
    }

    private String sanitizeMcqQuestion(String text) {
        if (text == null || text.isBlank()) return text;
        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String l = line.trim();
            String lower = l.toLowerCase();
            if (lower.startsWith("answer:") || lower.startsWith("correct answer") || lower.startsWith("correct:")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        String cleaned = sb.toString().trim();
        // Also strip any trailing parenthetical hints like "(Correct: X)" at the end
        cleaned = cleaned.replaceAll("\\(\\s*correct\\s*:\\s*[a-dA-D]\\s*\\)$", "");
        return cleaned.trim();
    }

    public String evaluateAnswer(String role,
                                 String question,
                                 String userAnswer,
                                 String questionType,
                                 String topicContext) {
        try {
            String systemPrompt = PromptTemplates.buildSystemPrompt(role);
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content",
                            PromptTemplates.evaluateAnswerPrompt(questionType, topicContext, question, userAnswer))
            );

            String response = sendGrokWithRetries(messages, Map.of("model", "grok-beta"));
            if (response != null && !response.isBlank() && !response.startsWith("ERROR_QUOTA_EXCEEDED:")) {
                log.debug("Evaluated answer via Grok for role: {}, question type: {}", role, questionType);
                return cleanJsonResponse(response);
            } else {
                log.warn("Grok returned empty or quota exceeded for evaluateAnswer — using local evaluation fallback");
                return evaluateFallback(question, userAnswer, questionType, topicContext);
            }
        } catch (Exception e) {
            log.warn("Grok evaluation failed: {}. Using local fallback.", e.getMessage());
            return evaluateFallback(question, userAnswer, questionType, topicContext);
        }
    }

    public String generateCodingProblem(String role, String topic, String difficulty) {
        try {
            String systemPrompt = PromptTemplates.buildSystemPrompt(role);
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content",
                            PromptTemplates.generateCodingProblemPrompt(topic, difficulty))
            );

            String response = sendGrokWithRetries(messages, Map.of("model", "grok-beta"));
            if (response != null && !response.isBlank() && !response.startsWith("ERROR_QUOTA_EXCEEDED:")) {
                log.debug("Generated coding problem via Grok for role: {}, topic: {}", role, topic);
                return response.trim();
            }
            // If Grok returned empty, fall back to local generator so the UI remains usable
            log.warn("Grok returned empty or quota exceeded for generateCodingProblem");
            return generateFallbackQuestion(role, topic, difficulty, "CODING", "INTERVIEW");
        } catch (Exception e) {
            log.warn("Grok coding problem generation failed: {}. Using fallback.", e.getMessage());
            return generateFallbackQuestion(role, topic, difficulty, "CODING", "INTERVIEW");
        }
    }

    public String evaluateCodingSolution(String role,
                                         String problem,
                                         String code,
                                         String output,
                                         String hiddenTestsDescription) {
        try {
            String systemPrompt = PromptTemplates.buildSystemPrompt(role);
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content",
                            PromptTemplates.evaluateCodingSolutionPrompt(problem, code, output, hiddenTestsDescription))
            );

            String response = sendGrokWithRetries(messages, Map.of("model", "grok-beta"));
            if (response != null && !response.isBlank() && !response.startsWith("ERROR_QUOTA_EXCEEDED:")) {
                log.debug("Evaluated coding solution via Grok for role: {}", role);
                return cleanJsonResponse(response);
            } else {
                log.warn("Grok returned empty or quota exceeded for evaluateCodingSolution — using local evaluation fallback");
                return evaluateCodingFallback(problem, code, output, hiddenTestsDescription);
            }
        } catch (Exception e) {
            log.warn("Grok coding evaluation failed: {}. Using local fallback.", e.getMessage());
            return evaluateCodingFallback(problem, code, output, hiddenTestsDescription);
        }
    }

    /**
    * Send messages to the Grok endpoint with retries/backoff and return the assistant content when available.
     */
    private String sendGrokWithRetries(List<Map<String, String>> messages, Map<String, String> options) {
        int attempt = 0;
        String lastError = null;

        while (attempt < Math.max(1, grokMaxRetries)) {
            attempt++;
            try {
                log.debug("Calling Grok (attempt {}/{}) with timeout {}s", attempt, grokMaxRetries, grokTimeoutSeconds);
                // Ensure options include model; send options through so temperature and others are honored
                String result = sendGrokRequest(messages, options);
                
                // If result is a special error indicator from sendGrokRequest, return it or handle it
                if (result != null && result.startsWith("ERROR_QUOTA_EXCEEDED:")) {
                     return result;
                }
                
                if (result != null) return result;
            } catch (Exception e) {
                lastError = e.getMessage();
                log.warn("Grok call failed on attempt {}/{}: {}", attempt, grokMaxRetries, e.getMessage());
                // If 403 or quota error, stop retrying immediately
                if (e.getMessage().contains("403") || e.getMessage().contains("Quota") || e.getMessage().contains("credits")) {
                     return "ERROR_QUOTA_EXCEEDED: Your xAI API key has no credits/licenses. Please verify billing at console.x.ai.";
                }
            }

            if (attempt < grokMaxRetries) {
                try {
                    Thread.sleep(500L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        log.warn("All {} Grok attempts failed/timeout. Last error: {}", grokMaxRetries, lastError);
        return null;
    }

    /**
    * Perform the actual HTTP request to the Grok API endpoint and parse the response.
    * This method tolerates multiple response shapes used by different providers.
     */
    @SuppressWarnings("unchecked")
    private String sendGrokRequest(List<Map<String, String>> messages, Map<String, String> options) throws IOException {
        String model = options.getOrDefault("model", "grok-beta");

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", messages,
                "stream", false
        );

        // If a temperature option is provided, build a mutable map to include it
        java.util.Map<String, Object> mutable = new java.util.HashMap<>(payload);
        String tempStr = options.get("temperature");
        if (tempStr != null) {
            try {
                double temp = Double.parseDouble(tempStr);
                mutable.put("temperature", temp);
            } catch (NumberFormatException ignored) {
            }
        }

        payload = mutable;

        String respBody = null;
        try {
                WebClient.RequestBodySpec req = webClient.post()
                    .uri(URI.create(grokBaseUrl.replaceAll("/+$", "") + "/v1/chat/completions"))
                    .contentType(MediaType.APPLICATION_JSON);

                // Add Authorization header from environment if available
                String apiKey = System.getenv("X_API_KEY");
                if (apiKey == null || apiKey.isBlank()) {
                    apiKey = System.getenv("GROK_API_KEY");
                }
                
                if (apiKey != null && !apiKey.isBlank()) {
                    String finalKey = apiKey;
                    req = req.headers(h -> h.setBearerAuth(finalKey));
                }

                respBody = req.bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.value() == 403 || status.value() == 401, response -> {
                        return response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Grok Auth Error {}: {}", response.statusCode(), body);
                            return reactor.core.publisher.Mono.error(new RuntimeException("Quota Exceeded or Invalid Key (403/401)"));
                        });
                    })
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(Math.max(1, grokTimeoutSeconds)));
        } catch (Exception we) {
            log.warn("WebClient call to Grok failed: {}", we.getMessage());
            
            // If it's the quota error, throw it up so the retry loop catches it and returns the special message
            if (we.getMessage().contains("Quota") || we.getMessage().contains("403")) {
                throw we;
            }

            // try a direct HttpClient as a secondary attempt in case WebClient is misbehaving
            try {
                respBody = tryHttpClientRequest(payload);
                log.debug("Grok response via HttpClient fallback: {}", respBody == null ? "<null>" : "(body len=" + respBody.length() + ")");
            } catch (Exception he) {
                log.warn("HttpClient fallback to Grok failed: {}", he.getMessage());
                if (he.getMessage().contains("403") || he.getMessage().contains("Quota")) {
                     throw new RuntimeException("Quota Exceeded (403)");
                }
            }
        }

        if (respBody == null) {
            log.warn("Grok returned empty body");
            return null;
        }

        // Check if body contains error structure even with 200 OK (unlikely but possible)
        if (respBody.contains("\"error\"") && respBody.contains("\"code\"")) {
             log.warn("Grok returned error in body: {}", respBody);
             return null;
        }

        log.debug("Grok response body: {}", respBody);

        Map<String, Object> map = mapper.readValue(respBody, new TypeReference<>() {});

        // Handle OpenAI-compatible response format (standard for xAI now)
        Object choicesObj = map.get("choices");
        if (choicesObj instanceof List choices && !choices.isEmpty()) {
            Object first = choices.get(0);
            if (first instanceof Map firstMap) {
                Object msg = firstMap.get("message");
                if (msg instanceof Map msgMap) {
                    Object content = msgMap.get("content");
                    if (content != null) return String.valueOf(content);
                }
                // Fallback for some non-standard responses
                Object direct = firstMap.get("content");
                if (direct != null) return String.valueOf(direct);
                Object text = firstMap.get("text");
                if (text != null) return String.valueOf(text);
            }
        }
        
        // Fallback for legacy/other formats
        Object out = map.get("output");
        if (out instanceof String s) return s;
        Object text = map.get("text");
        if (text instanceof String ts) return ts;

        Object messageObj = map.get("message");
        if (messageObj instanceof Map messageMap) {
            Object content = messageMap.get("content");
            if (content != null) return String.valueOf(content);
        }

        // No recognized content found
        log.warn("Grok response did not contain a recognized message content: {}", respBody);
        return null;
    }

    /**
     * Clean JSON response from AI (remove markdown fences if present).
     */
    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";

        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
        return cleaned.trim();
    }

    /**
    * Secondary HTTP client fallback for calling Grok when WebClient fails.
     */
    private String tryHttpClientRequest(Map<String, Object> payload) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.max(1, grokTimeoutSeconds)))
            .build();

        String json = mapper.writeValueAsString(payload);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(grokBaseUrl.replaceAll("/+$", "") + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(Math.max(1, grokTimeoutSeconds)))
                .header("Content-Type", "application/json")
                ;

        // Add Authorization header if present
        String apiKey = System.getenv("X_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GROK_API_KEY");
        }
        
        if (apiKey != null && !apiKey.isBlank()) {
            reqBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest req = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return resp.body();
        }
        log.warn("HttpClient request to Grok returned status {}", resp.statusCode());
        return null;
    }

    // --- Local evaluation fallbacks ---
    private String evaluateFallback(String question, String userAnswer, String questionType, String topicContext) {
        if (userAnswer == null) userAnswer = "";
        String ans = userAnswer.trim();
        double score = 0.0;
        String feedback;
        String suggestions = "Provide a more detailed answer with examples and key points.";

        int len = ans.length();
        if (len > 300) score = 9.0;
        else if (len > 150) score = 7.0;
        else if (len > 80) score = 6.0;
        else if (len > 30) score = 4.0;
        else if (len > 5) score = 2.0;
        else score = 0.0;

        if ("MCQ".equalsIgnoreCase(questionType)) {
            String cleaned = ans.trim().toUpperCase();
            String selected = null;
            if (cleaned.matches("^[A-D]\\)?\\.?$")) selected = cleaned.substring(0,1);
            else if (cleaned.matches("^OPTION\\s+[A-D]$")) selected = cleaned.substring(cleaned.length()-1);

            String[] derived = deriveMcqCorrectAnswer(question);
            String correct = derived[0];
            String reason = derived[1];

            boolean canJudge = correct != null && selected != null;
            boolean isCorrect = canJudge && correct.equalsIgnoreCase(selected);
            if (canJudge) {
                score = isCorrect ? 8.0 : 3.0;
                feedback = isCorrect ? "Correct choice for this standard MCQ." : "Selected option does not match the expected answer.";
                return String.format("{\"score\":%.1f,\"feedback\":\"%s\",\"explanation\":\"%s\",\"suggestions\":\"%s\",\"is_correct\":%s,\"correct_answer\":\"%s\",\"reason\":\"%s\"}",
                        score,
                        escapeJson(feedback),
                        "",
                        escapeJson(suggestions),
                        isCorrect ? "true" : "false",
                        escapeJson(correct),
                        escapeJson(reason != null ? reason : ""));
            } else {
                score = Math.max(score, 6.0);
                feedback = "Recorded your selected option. Full AI scoring unavailable locally.";
                return String.format("{\"score\":%.1f,\"feedback\":\"%s\",\"explanation\":\"%s\",\"suggestions\":\"%s\",\"is_correct\":false,\"correct_answer\":\"\",\"reason\":\"Local evaluator cannot determine the correct option without the AI service.\"}",
                        score,
                        escapeJson(feedback),
                        "",
                        escapeJson(suggestions));
            }
        }

        if (score >= 7.0) {
            feedback = "Good answer — covers most points concisely.";
            suggestions = "Polish with more concrete examples and edge-cases for full marks.";
        } else if (score >= 4.0) {
            feedback = "Partial answer — you touched some points but missed details.";
        } else if (score > 0) {
            feedback = "Short answer — expand with definitions and examples.";
        } else {
            feedback = "No answer provided or too short to evaluate.";
        }

        String correct = (score >= 7.0)
                ? "Covers expected key points for this topic"
                : "Provide an ideal outline: definitions, practical steps, trade‑offs, and examples";
        String reason = (score >= 7.0)
                ? "Answer aligns with core concepts and practical expectations"
                : "Insufficient detail; include key concepts, examples, and trade‑offs";
        String[] ideal = deriveSubjectiveIdealAnswer(question);
        if (ideal[0] != null) correct = ideal[0];
        if (ideal[1] != null) reason = ideal[1];

        return String.format("{\"score\":%.1f,\"feedback\":\"%s\",\"explanation\":\"%s\",\"suggestions\":\"%s\",\"is_correct\":%s,\"correct_answer\":\"%s\",\"reason\":\"%s\"}",
                score,
                escapeJson(feedback),
                "",
                escapeJson(suggestions),
                (score >= 7.0 ? "true" : "false"),
                escapeJson(correct),
                escapeJson(reason));
    }

    private String[] deriveMcqCorrectAnswer(String questionText) {
        if (questionText == null) return new String[]{null, null};
        String q = questionText.toLowerCase();
        if (q.contains("binary search")) return new String[]{"B", "Binary search halves the search space each step => O(log n)."};
        if (q.contains("idempotent") && q.contains("http")) return new String[]{"B", "PUT is defined as idempotent — repeating requests yield the same state."};
        if (q.contains("fifo") || q.contains("first in first out")) return new String[]{"B", "Queue provides FIFO ordering."};
        if (q.contains("filters rows before aggregation")) return new String[]{"C", "WHERE filters rows before GROUP BY; HAVING filters after aggregation."};
        if (q.contains("prevents subclassing")) return new String[]{"B", "final prevents inheritance for classes."};
        return new String[]{null, null};
    }
    private String[] deriveSubjectiveIdealAnswer(String questionText) {
        if (questionText == null) return new String[]{null, null};
        String q = questionText.toLowerCase();
        if (q.contains("relational schema") && (q.contains("e‑commerce") || q.contains("e-commerce"))) {
            String answer = String.join("\n",
                    "Tables: users(id,email), orders(id,user_id,status,created_at), order_items(id,order_id,product_id,qty,price), products(id,name,category), payments(id,order_id,amount,status,method).",
                    "Relationships: users 1‑N orders; orders 1‑N order_items; products 1‑N order_items; orders 1‑1 payments.",
                    "Query1: SELECT SUM(qty*price) FROM order_items WHERE order_id=?;",
                    "Query2: SELECT product_id, SUM(qty) AS sold FROM order_items GROUP BY product_id ORDER BY sold DESC LIMIT 5;",
                    "Indexing: orders(user_id,created_at), order_items(order_id), products(category) — faster reads vs higher write cost.");
            String reason = "Covers normalized tables, key FKs, example analytics queries, and indexing trade‑offs.";
            return new String[]{answer, reason};
        }
        if (q.contains("@transactional") || q.contains("transaction management")) {
            String answer = String.join("\n",
                    "Use @Transactional at service layer; default propagation REQUIRED and isolation READ_COMMITTED.",
                    "Rollbacks on RuntimeException by default; use rollbackFor for checked exceptions.",
                    "Pitfalls: lazy loading outside txn, N+1 queries; prefer fetch joins and bounded transactions.",
                    "Example: @Transactional public void placeOrder(){ save(order); save(items); charge(payment); }",
                    "Ensure idempotency and proper exception mapping.");
            String reason = "Addresses propagation/isolation, rollback behavior, pitfalls, and a concise example.";
            return new String[]{answer, reason};
        }
        if (q.contains("secure") && (q.contains("react") && q.contains("spring boot"))) {
            String answer = String.join("\n",
                    "Auth: login endpoint issues JWT; React stores token in memory; attach Authorization: Bearer.",
                    "Backend: stateless security, disable CSRF for APIs, validate JWT, set role-based access via authorities.",
                    "Refresh/expiry handling: short-lived access tokens + refresh endpoint.",
                    "Protect sensitive routes in React via guards; avoid localStorage for highly sensitive contexts.",
                    "Audit/monitor login attempts and failures.");
            String reason = "Provides end-to-end flow with JWT, CSRF rules, roles, and client guards.";
            return new String[]{answer, reason};
        }
        if (q.contains("debug") && q.contains("slow") && q.contains("checkout")) {
            String answer = String.join("\n",
                    "Observe: check logs, metrics (latency, DB), and traces to locate bottlenecks.",
                    "Profile DB: EXPLAIN, look for full scans; add indexes or optimize joins.",
                    "Profile code: hotspots, caching, reduce remote calls; batch operations.",
                    "Rollout fix: canary, monitor errors/latency; rollback on regressions.",
                    "Postmortem: add alerts and budgets.");
            String reason = "Covers observability, DB/code profiling, safe rollout, and follow-ups.";
            return new String[]{answer, reason};
        }
        if (q.contains("design a rest api") && q.contains("order")) {
            String answer = String.join("\n",
                    "Resources: /orders, /orders/{id}, /users/{id}/orders; JSON input/output.",
                    "Status codes: 201 create, 200 read/update, 204 delete; 400/404/409 for errors.",
                    "Validation: request schemas; pagination via page/size/sort.",
                    "Errors: consistent problem+json; auth via JWT and roles.",
                    "Versioning: /v1 and ETag for concurrency.");
            String reason = "Describes endpoints, status codes, validation, errors, auth, and versioning.";
            return new String[]{answer, reason};
        }
        return new String[]{null, null};
    }
    private String evaluateCodingFallback(String problem, String code, String output, String hiddenTestsDescription) {
        double score = 0.0;
        String feedback;
        String suggestions = "Run more tests locally and ensure edge cases are handled.";

        if (code == null || code.trim().isEmpty()) {
            feedback = "No code submitted.";
            score = 0.0;
        } else if (output != null && !output.trim().isEmpty()) {
            feedback = "Code executed and produced output — basic checks passed.";
            score = 6.0;
            suggestions = "Add more unit tests and handle error cases for higher score.";
        } else {
            feedback = "Code submitted but no execution output available.";
            score = 4.0;
        }

        return String.format("{\"score\":%.1f,\"feedback\":\"%s\",\"explanation\":\"%s\",\"suggestions\":\"%s\",\"is_correct\":%s,\"correct_answer\":\"%s\",\"reason\":\"%s\"}",
                score,
                escapeJson(feedback),
                "",
                escapeJson(suggestions),
                (score >= 7.0 ? "true" : "false"),
                escapeJson("Implement optimal algorithm and handle edge cases; verify outputs against sample tests"),
                escapeJson("Solution correctness depends on matching expected behavior and complexity; add tests to confirm"));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
