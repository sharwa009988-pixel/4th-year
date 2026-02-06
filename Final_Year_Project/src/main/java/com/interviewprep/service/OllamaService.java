package com.interviewprep.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final WebClient webClient;
    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final double temperature;

    public OllamaService(
            @Value("${spring.ai.grok.base-url:https://api.grok.x.ai}") String baseUrl,
            @Value("${spring.ai.grok.endpoint:/v1/generate}") String endpoint,
            @Value("${spring.ai.grok.api-key:}") String apiKey,
            @Value("${spring.ai.grok.model:grokxai}") String model,
            @Value("${spring.ai.grok.chat.temperature:0.7}") double temperature
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
        this.temperature = temperature;
    }

    /**
     * Generate a single assistant response using Grok (xAI).
     * This replaces local Ollama calls. The implementation constructs
     * a simple prompt by joining conversation messages and sends a
     * JSON payload to the configured Grok endpoint. The code is written
     * defensively to attempt extracting the assistant text from common
     * response shapes.
     */
    public String generateSingleQuestion(String systemPrompt, List<Map<String, String>> conversationMessages, Map<String, Object> options) {
        String usedModel = String.valueOf(options.getOrDefault("model", this.model));

        // Build a single prompt string from messages (role/content pairs)
        StringBuilder prompt = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            prompt.append("System: ").append(systemPrompt).append("\n\n");
        }
        for (Map<String, String> m : conversationMessages) {
            String role = m.getOrDefault("role", "user");
            String content = m.getOrDefault("content", "");
            prompt.append(role).append(": ").append(content).append("\n");
        }

        Map<String, Object> payload = Map.of(
                "model", usedModel,
                "input", prompt.toString(),
                "temperature", options.getOrDefault("temperature", this.temperature)
        );

        WebClient.RequestBodySpec req = webClient.post()
                .uri(this.endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.apiKey != null && !this.apiKey.isBlank() ? "Bearer " + this.apiKey : "");

        Mono<Map> resp = req.body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30));

        Map body = resp.block(Duration.ofSeconds(35));
        if (body == null) return "";

        // Try common response shapes (grok/xai may return different formats)
        // 1) { "output": "..." }
        Object out = body.get("output");
        if (out instanceof String s) return s;

        // 2) { "text": "..." }
        Object text = body.get("text");
        if (text instanceof String ts) return ts;

        // 3) choices -> [{ text: "..." }] or choices -> [{ message: { content: "..." } }]
        Object choices = body.get("choices");
        if (choices instanceof List cl && !cl.isEmpty()) {
            Object first = cl.get(0);
            if (first instanceof Map fm) {
                Object t = fm.get("text");
                if (t instanceof String) return (String) t;
                Object msg = fm.get("message");
                if (msg instanceof Map mm) {
                    Object content = mm.get("content");
                    if (content instanceof String) return (String) content;
                }
            }
        }

        // 4) data -> [{ "text": "..." }]
        Object data = body.get("data");
        if (data instanceof List dl && !dl.isEmpty()) {
            Object first = dl.get(0);
            if (first instanceof Map fm) {
                Object t = fm.get("text");
                if (t instanceof String) return (String) t;
            }
        }

        // Fallback to entire body
        return body.toString();
    }
}
