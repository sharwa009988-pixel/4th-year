package com.interviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewprep.dto.CodeExecutionRequest;
import com.interviewprep.dto.CodeExecutionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CodeExecutionService {

    @Value("${jdoodle.client-id}")
    private String clientId;

    @Value("${jdoodle.client-secret}")
    private String clientSecret;

    @Value("${jdoodle.api-url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public CodeExecutionService(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    /**
     * Execute Java code using JDoodle API
     */
    public CodeExecutionResponse executeCode(CodeExecutionRequest request) {
        try {
            // Basic validation: if JDoodle credentials are missing or left as placeholders,
            // return a friendly error instead of attempting the remote call which yields 403.
            if (clientId == null || clientId.isBlank() || clientId.contains("your-jdoodle-client-id")
                || clientSecret == null || clientSecret.isBlank() || clientSecret.contains("your-jdoodle-client-secret")) {
            String msg = "JDoodle client credentials are not configured. Set JDOODLE_CLIENT_ID and JDOODLE_CLIENT_SECRET environment variables or update backend/src/main/resources/application.yml.";
            log.warn(msg);
            return CodeExecutionResponse.builder()
                .error(msg)
                .statusCode(400)
                .build();
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("clientId", clientId);
            payload.put("clientSecret", clientSecret);
            payload.put("script", request.getCode());
            payload.put("stdin", request.getStdin() != null ? request.getStdin() : "");
            payload.put("language", "java");
            payload.put("versionIndex", "4"); // Java 17

                log.info("Executing code via JDoodle API");

                try {
                String response = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

                log.info("JDoodle API response: {}", response);

                JsonNode jsonNode = objectMapper.readTree(response);

                return CodeExecutionResponse.builder()
                    .output(jsonNode.has("output") && !jsonNode.get("output").isNull() ? jsonNode.get("output").asText() : "")
                    .error(jsonNode.has("error") && !jsonNode.get("error").isNull() ? jsonNode.get("error").asText() : "")
                    .statusCode(jsonNode.has("statusCode") && !jsonNode.get("statusCode").isNull() ? jsonNode.get("statusCode").asInt() : 200)
                    .memory(jsonNode.has("memory") && !jsonNode.get("memory").isNull() ? jsonNode.get("memory").asText() : "")
                    .cpuTime(jsonNode.has("cpuTime") && !jsonNode.get("cpuTime").isNull() ? jsonNode.get("cpuTime").asText() : "")
                    .build();
                } catch (WebClientResponseException wcre) {
                // Log detailed response information for non-2xx responses
                log.error("JDoodle WebClient error. Status: {}, Headers: {}, Body: {}",
                    wcre.getStatusCode().value(), wcre.getHeaders(), wcre.getResponseBodyAsString());

                // Try a fallback using java.net.http.HttpClient to capture the raw response
                try {
                    String jsonPayload = objectMapper.writeValueAsString(payload);
                    HttpClient httpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                        .build();

                    HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                    log.info("JDoodle HttpClient fallback response. Status: {}, Body: {}",
                        httpResponse.statusCode(), httpResponse.body());

                    if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                    JsonNode jsonNode = objectMapper.readTree(httpResponse.body());
                    return CodeExecutionResponse.builder()
                        .output(jsonNode.has("output") && !jsonNode.get("output").isNull() ? jsonNode.get("output").asText() : "")
                        .error(jsonNode.has("error") && !jsonNode.get("error").isNull() ? jsonNode.get("error").asText() : "")
                        .statusCode(jsonNode.has("statusCode") && !jsonNode.get("statusCode").isNull() ? jsonNode.get("statusCode").asInt() : httpResponse.statusCode())
                        .memory(jsonNode.has("memory") && !jsonNode.get("memory").isNull() ? jsonNode.get("memory").asText() : "")
                        .cpuTime(jsonNode.has("cpuTime") && !jsonNode.get("cpuTime").isNull() ? jsonNode.get("cpuTime").asText() : "")
                        .build();
                    } else {
                    return CodeExecutionResponse.builder()
                        .error("JDoodle returned status " + httpResponse.statusCode() + ": " + httpResponse.body())
                        .statusCode(httpResponse.statusCode())
                        .build();
                    }
                } catch (IOException | InterruptedException ioe) {
                    log.error("HttpClient fallback failed: {}", ioe.getMessage(), ioe);
                    return CodeExecutionResponse.builder()
                        .error("JDoodle call failed: " + ioe.getMessage())
                        .statusCode(502)
                        .build();
                }
                } catch (Exception we) {
                // DNS/Network errors from WebClient (e.g., WebClientRequestException) will be caught here
                log.warn("WebClient call to JDoodle failed: {}", we.getMessage());
                try {
                    String jsonPayload = objectMapper.writeValueAsString(payload);
                    HttpClient httpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                        .build();
                    HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
                    log.info("JDoodle HttpClient fallback response. Status: {}, Body: {}",
                        httpResponse.statusCode(), httpResponse.body());
                    if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                        JsonNode jsonNode = objectMapper.readTree(httpResponse.body());
                        return CodeExecutionResponse.builder()
                                .output(jsonNode.has("output") && !jsonNode.get("output").isNull() ? jsonNode.get("output").asText() : "")
                                .error(jsonNode.has("error") && !jsonNode.get("error").isNull() ? jsonNode.get("error").asText() : "")
                                .statusCode(httpResponse.statusCode())
                                .memory(jsonNode.has("memory") && !jsonNode.get("memory").isNull() ? jsonNode.get("memory").asText() : "")
                                .cpuTime(jsonNode.has("cpuTime") && !jsonNode.get("cpuTime").isNull() ? jsonNode.get("cpuTime").asText() : "")
                                .build();
                    }
                    return CodeExecutionResponse.builder()
                            .error("JDoodle returned status " + httpResponse.statusCode() + ": " + httpResponse.body())
                            .statusCode(httpResponse.statusCode())
                            .build();
                } catch (Exception he) {
                    log.error("HttpClient fallback to JDoodle failed: {}", he.getMessage(), he);
                    return CodeExecutionResponse.builder()
                            .error("Network/DNS error calling JDoodle: " + he.getMessage())
                            .statusCode(503)
                            .build();
                }
                }

        } catch (Exception e) {
            log.error("Error executing code: {}", e.getMessage(), e);
            return CodeExecutionResponse.builder()
                    .error("Error executing code: " + e.getMessage())
                    .statusCode(500)
                    .build();
        }
    }
}
