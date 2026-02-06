package com.interviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Execute Java code via JDoodle API (sandbox).
 * See https://www.jdoodle.com/compiler-api
 */
@Service
public class JdoodleService {

    private static final String EXECUTE_URL = "https://api.jdoodle.com/v1/execute";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.jdoodle.client-id:}")
    private String clientId;

    @Value("${app.jdoodle.client-secret:}")
    private String clientSecret;

    public CodeExecutionResult execute(String code, String language, String stdin) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return CodeExecutionResult.error("JDoodle API not configured. Set app.jdoodle.client-id and client-secret.");
        }
        try {
            String jdoodleLang = mapLanguage(language);
            String versionIndex = defaultVersionIndexFor(jdoodleLang);
            Map<String, Object> body = Map.of(
                    "clientId", clientId,
                    "clientSecret", clientSecret,
                    "script", code,
                    "language", jdoodleLang,
                    "versionIndex", versionIndex,
                    "stdin", stdin != null ? stdin : ""
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(EXECUTE_URL, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String output = root.has("output") ? root.get("output").asText() : "";
            String error = root.has("error") ? root.get("error").asText() : null;
            if (error != null && !error.isBlank()) {
                return CodeExecutionResult.error(error);
            }
            return CodeExecutionResult.success(output);
        } catch (Exception e) {
            return CodeExecutionResult.error("Execution failed: " + e.getMessage());
        }
    }

    private String mapLanguage(String language) {
        if (language == null) return "java";
        switch (language.toUpperCase()) {
            case "JAVA": return "java";
            case "PYTHON": return "python3";
            case "CPP": return "cpp17";
            case "C": return "c";
            default: return "java";
        }
    }

    private String defaultVersionIndexFor(String jdoodleLang) {
        switch (jdoodleLang) {
            case "java": return "5";
            case "python3": return "3";
            case "cpp17": return "1";
            case "c": return "5";
            default: return "0";
        }
    }

    public static class CodeExecutionResult {
        private final boolean success;
        private final String output;
        private final String error;

        private CodeExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public static CodeExecutionResult success(String output) {
            return new CodeExecutionResult(true, output != null ? output : "", null);
        }

        public static CodeExecutionResult error(String error) {
            return new CodeExecutionResult(false, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getError() { return error; }
    }
}
