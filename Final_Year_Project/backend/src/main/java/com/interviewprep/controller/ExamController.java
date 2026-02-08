package com.interviewprep.controller;

import com.interviewprep.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController {

    private final AiService aiService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exam(@RequestBody Map<String, Object> body) {
        String pageType = String.valueOf(body.getOrDefault("page_type", "subjective")).trim().toLowerCase();
        String userPrompt = String.valueOf(body.getOrDefault("user_prompt", "")).trim();
        String userAnswer = String.valueOf(body.getOrDefault("user_answer", "")).trim();

        String system = buildSystemPrompt(pageType);
        String request = buildUserPrompt(pageType, userPrompt, userAnswer);

        double temp = pageType.equals("mcq") ? 0.3 : (pageType.equals("coding") ? 0.2 : 0.5);
        String json = aiService.callGeminiJson(system, request, temp);
        if (json == null || json.isBlank() || isEmptyJson(json)) {
            Map<String, Object> fallback = new HashMap<>();
            Map<String, Object> question = new HashMap<>();
            if ("mcq".equals(pageType)) {
                question.put("text", "Which HTTP method is idempotent by definition?");
                java.util.List<Map<String, String>> options = new java.util.ArrayList<>();
                options.add(Map.of("key", "A", "text", "POST"));
                options.add(Map.of("key", "B", "text", "PUT"));
                options.add(Map.of("key", "C", "text", "PATCH"));
                options.add(Map.of("key", "D", "text", "CONNECT"));
                question.put("options", options);
                question.put("answer", "B");
            } else if ("coding".equals(pageType)) {
                question.put("text", "Write a function that reverses a string.");
                java.util.List<Map<String, String>> examples = new java.util.ArrayList<>();
                examples.add(Map.of("input", "hello", "output", "olleh"));
                examples.add(Map.of("input", "abc", "output", "cba"));
                question.put("examples", examples);
            } else if ("fullmock".equals(pageType)) {
                question.put("text", "Describe ACID properties and their importance.");
            } else {
                question.put("text", "Explain SOLID principles with practical Java examples.");
            }
            java.util.List<Map<String, Object>> questions = new java.util.ArrayList<>();
            questions.add(question);
            fallback.put("questions", questions);
            Map<String, Object> eval = new HashMap<>();
            eval.put("score", 0);
            eval.put("feedback", "Evaluation unavailable. Using local fallback content.");
            eval.put("is_correct", false);
            eval.put("correct_answer", "");
            eval.put("explanation", "");
            fallback.put("evaluation", eval);
            fallback.put("refined", "");
            String fallbackJson = toJson(fallback);
            return ResponseEntity.ok(fallbackJson);
        }
        return ResponseEntity.ok(json);
    }

    private String buildSystemPrompt(String pageType) {
        String base = "You are a senior technical interviewer. Return ONLY JSON per schema. Keep outputs recruiter-ready, clear, and concise.";
        String schema = """
{"questions":[{"type":"string","text":"string","options":[{"key":"string","text":"string"}],"answer":"string","examples":[{"input":"string","output":"string"}]}],"evaluation":{"score":"number","feedback":"string","is_correct":"boolean","correct_answer":"string","explanation":"string"},"refined":"string"}""";
        String typeSpec;
        switch (pageType) {
            case "mcq":
                typeSpec = "For MCQ: each question has exactly 4 options A-D; include the correct answer letter in 'answer'.";
                break;
            case "coding":
                typeSpec = "For coding: include input/output examples per question in 'examples'.";
                break;
            case "fullmock":
                typeSpec = "Full Mock: generate a balanced mix — include some subjective, some MCQ, and at least one coding task.";
                break;
            default:
                typeSpec = "Subjective: generate open-ended, analytical questions.";
        }
        return base + " Schema: " + schema + " " + typeSpec;
    }

    private String buildUserPrompt(String pageType, String userPrompt, String userAnswer) {
        String header = "Page Type: " + pageType.toUpperCase() + "\nInstructions: " + (userPrompt == null ? "" : userPrompt);
        String eval = "";
        if (userAnswer != null && !userAnswer.isBlank()) {
            eval = "\nCandidate Submission:\n" + userAnswer + "\nEvaluate the submission and fill 'evaluation'.";
            if ("coding".equalsIgnoreCase(pageType)) {
                eval += " Focus evaluation on algorithm correctness, complexity, and edge-cases.";
            }
            if ("mcq".equalsIgnoreCase(pageType)) {
                eval += " Compare candidate option with correct 'answer'.";
            }
        }
        return header + "\nGenerate 'questions' and if submission present, produce 'evaluation'. Provide a 'refined' improved version when applicable." + eval;
    }

    private String toJson(Map<String, Object> map) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
            return m.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"questions\":[],\"evaluation\":{\"score\":0,\"feedback\":\"\"},\"refined\":\"\"}";
        }
    }

    private boolean isEmptyJson(String s) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode n = m.readTree(s);
            if (n == null || n.isNull()) return true;
            com.fasterxml.jackson.databind.JsonNode q = n.get("questions");
            if (q != null && q.isArray() && q.size() > 0) return false;
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
