package com.interviewprep.controller;

import com.interviewprep.dto.AnswerRequest;
import com.interviewprep.dto.EvaluateRequest;
import com.interviewprep.dto.FeedbackResponse;
import com.interviewprep.dto.InterviewRequest;
import com.interviewprep.dto.InterviewStartResponse;
import com.interviewprep.dto.SessionSummaryResponse;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.SessionQuestion;
import com.interviewprep.entity.User;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.SessionQuestionRepository;
import com.interviewprep.repository.UserRepository;
import com.interviewprep.service.AiService;
import com.interviewprep.service.InterviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/interviews", "/api/interview"})
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final InterviewSessionRepository interviewSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    @PostMapping("/start")
    public ResponseEntity<InterviewStartResponse> startInterview(
            @Valid @RequestBody InterviewRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = getUserIdFromEmail(email);
        InterviewSession session = interviewService.createSession(userId, request);
        InterviewStartResponse response = new InterviewStartResponse(
                session.getId(),
                session.getType().name(),
                session.getTopic()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/question/generate")
    public ResponseEntity<Map<String, String>> generateQuestion(
            @RequestBody Map<String, Object> req,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = getUserIdFromEmail(email);
        String role = userRepository.findById(userId).map(u -> u.getRole()).orElse(null);

        String mode = req.getOrDefault("mode", "SUBJECTIVE").toString();
        String topic = req.getOrDefault("topic", null) == null ? null : req.get("topic").toString();
        String difficulty = req.getOrDefault("difficulty", "MEDIUM").toString();

        String questionText = aiService.generateQuestion(role, topic, difficulty, mode, mode);
        if (questionText != null) {
            // Sanitize any leading tags the model might include
            questionText = questionText
                    .replaceFirst("^\\s*\\((?:MCQ|SUBJECTIVE|CODING|BEHAVIORAL)\\)\\s*", "")
                    .replaceFirst("^\\s*\\[(?:EASY|MEDIUM|HARD)\\]\\s*", "")
                    .replaceFirst("^\\s*[^:\\n]{3,50}:\\s+", "")
                    .trim();
        }

        // Try to persist question to the user's most recent active session (if any)
        Long savedQuestionId = null;
        try {
            var sessions = interviewSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
            if (sessions != null && !sessions.isEmpty()) {
                InterviewSession latest = sessions.get(0);
                if (latest.getEndTime() == null) {
                    SessionQuestion.QuestionType qt = SessionQuestion.QuestionType.SUBJECTIVE;
                    try {
                        qt = SessionQuestion.QuestionType.valueOf(mode != null ? mode.toUpperCase() : "SUBJECTIVE");
                    } catch (Exception e) {
                        qt = SessionQuestion.QuestionType.SUBJECTIVE;
                    }

                    SessionQuestion sq = SessionQuestion.builder()
                            .session(latest)
                            .questionText(questionText)
                            .type(qt)
                            .questionOrder(latest.getQuestions() != null ? latest.getQuestions().size() + 1 : 1)
                            .build();

                    SessionQuestion saved = sessionQuestionRepository.save(sq);
                    savedQuestionId = saved.getId();
                }
            }
        } catch (Exception e) {
            // Non-fatal — log and continue returning question text
            // Note: logging via System.out to ensure visibility in dev
            System.err.println("[WARN] Failed to persist generated question: " + e.getMessage());
        }

        if (savedQuestionId != null) {
            return ResponseEntity.ok(Map.of("question", questionText, "questionId", String.valueOf(savedQuestionId)));
        }

        return ResponseEntity.ok(Map.of("question", questionText));
    }

    @PostMapping("/coding/problem")
    public ResponseEntity<Map<String, String>> generateCodingProblem(
            @RequestBody(required = false) Map<String, Object> req,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = getUserIdFromEmail(email);
        String role = userRepository.findById(userId).map(u -> u.getRole()).orElse(null);
        if (role == null || role.isBlank()) {
            throw new RuntimeException("Please select your target role before starting an interview.");
        }
        String topic = req != null && req.get("topic") != null ? req.get("topic").toString() : null;
        String difficulty = req != null && req.get("difficulty") != null ? req.get("difficulty").toString() : "MEDIUM";

        // Delegate to AI service to generate coding problem
        String problem = aiService.generateCodingProblem(role, topic, difficulty);
        return ResponseEntity.ok(Map.of("problem", problem));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<FeedbackResponse> evaluateQuestion(@RequestBody EvaluateRequest req,
                                                            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        Long sessionId = req.getSessionId();

        Long questionId = req.getQuestionId();
        if (questionId == null) {
            var sessionSummary = interviewService.getSessionDetails(userId, sessionId);
            // Try to find exact matching question text first
            for (var q : sessionSummary.getQuestions()) {
                if (q.getQuestionText() != null && q.getQuestionText().equals(req.getQuestionText())) {
                    questionId = q.getQuestionId();
                    break;
                }
            }
            // If no exact match, pick the first unanswered question
            if (questionId == null) {
                for (var q : sessionSummary.getQuestions()) {
                    if (q.getScore() == null) {
                        questionId = q.getQuestionId();
                        break;
                    }
                }
            }
        }

        if (questionId == null) {
            throw new RuntimeException("No suitable question found to evaluate");
        }

        com.interviewprep.dto.AnswerRequest ar = new com.interviewprep.dto.AnswerRequest();
        ar.setQuestionId(questionId);
        ar.setAnswer(req.getUserAnswer());

        FeedbackResponse resp = interviewService.submitAnswer(userId, sessionId, ar);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/evaluate-code")
    public ResponseEntity<FeedbackResponse> evaluateCode(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        Long sessionId = body.get("sessionId") instanceof Number n ? n.longValue() : null;
        String problemStatement = body.get("problemStatement") != null ? body.get("problemStatement").toString() : "";
        String code = body.get("code") != null ? body.get("code").toString() : "";
        String executionOutput = body.get("executionOutput") != null ? body.get("executionOutput").toString() : "";
        String role = interviewSessionRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream().findFirst().map(s -> s.getUser().getRole()).orElse(null);

        String aiJson = aiService.evaluateCodingSolution(role, problemStatement, code, executionOutput, null);

        String feedback = aiJson;
        Double score = null;
        String explanation = "";
        String suggestions = "";
        Boolean isCorrect = null;
        String correctAnswer = "";
        String reason = "";
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(aiJson);
            if (node.has("feedback")) feedback = node.get("feedback").asText();
            if (node.has("score")) score = node.get("score").asDouble();
            if (node.has("explanation")) explanation = node.get("explanation").asText();
            if (node.has("suggestions")) suggestions = node.get("suggestions").asText();
            if (node.has("is_correct")) isCorrect = node.get("is_correct").asBoolean();
            if (node.has("correct_answer")) correctAnswer = node.get("correct_answer").asText();
            if (node.has("reason")) reason = node.get("reason").asText();
        } catch (Exception ignored) {}

        FeedbackResponse resp = FeedbackResponse.builder()
                .feedback(feedback)
                .score(score)
                .explanation(explanation)
                .suggestions(suggestions)
                .isCorrect(isCorrect)
                .correctAnswer(correctAnswer)
                .reason(reason)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<FeedbackResponse> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody AnswerRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        FeedbackResponse response = interviewService.submitAnswer(userId, sessionId, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummaryResponse>> getUserSessions(Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        List<SessionSummaryResponse> sessions = interviewService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionSummaryResponse> getSessionDetails(
            @PathVariable Long sessionId,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        SessionSummaryResponse session = interviewService.getSessionDetails(userId, sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(value = "role", required = false) String roleFilter,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        var sessions = interviewService.getUserSessions(userId);
        List<Map<String, Object>> mapped = new java.util.ArrayList<>();
        for (var s : sessions) {
            if (roleFilter != null && !roleFilter.isBlank()) {
                // best effort role snapshot (current user role)
                var userRole = userRepository.findById(userId).map(User::getRole).orElse(null);
                if (userRole == null || !userRole.toLowerCase().contains(roleFilter.toLowerCase())) {
                    continue;
                }
            }
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", s.getSessionId());
            row.put("startedAt", s.getStartTime());
            row.put("roleSnapshot", userRepository.findById(userId).map(User::getRole).orElse(null));
            row.put("sessionType", s.getType() != null ? s.getType().name() : null);
            row.put("topic", s.getTopic());
            row.put("score", s.getTotalScore());
            row.put("totalQuestions", s.getTotalQuestions());
            mapped.add(row);
            if (mapped.size() >= Math.max(1, limit)) break;
        }
        return ResponseEntity.ok(mapped);
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Void> endSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        interviewService.endSession(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}/export")
    public ResponseEntity<byte[]> exportSessionPdf(
            @PathVariable Long sessionId,
            Authentication authentication) {
        Long userId = getUserIdFromEmail(authentication.getName());
        byte[] pdfBytes = interviewService.exportSessionPdf(userId, sessionId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=interview-session-" + sessionId + ".pdf")
                .body(pdfBytes);
    }

    private Long getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
