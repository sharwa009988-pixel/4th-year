package com.interviewprep.controller;

import com.interviewprep.dto.InterviewRequest;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.service.InterviewService;
import com.interviewprep.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserService userService;

    public InterviewController(InterviewService interviewService, UserService userService) {
        this.interviewService = interviewService;
        this.userService = userService;
    }

    @PostMapping({"/start", "/interviews/start"})
    public ResponseEntity<InterviewSession> startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InterviewRequest request) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        // Delegate role check to the service which throws a meaningful exception
        InterviewSession session = interviewService.startSession(
                user.getId(),
                request.getMode(),
                request.getTopic(),
                request.getDifficulty(),
                request.getNumberOfQuestions(),
                request.getTimeLimitPerQuestionSeconds(),
                request.getTimeLimitOverallMinutes(),
                request.getProgrammingLanguage());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/question/generate")
    public ResponseEntity<Map<String, String>> generateQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> params) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        String role = user.getTargetRole();
        if (role == null || role.isBlank()) {
            throw new IllegalStateException("Please select your target role before starting an interview.");
        }
        String question = interviewService.generateQuestion(
                role,
                params.get("mode"),
                params.get("topic"),
                params.get("difficulty"));
        return ResponseEntity.ok(Map.of("question", question));
    }

    @PostMapping("/coding/problem")
    public ResponseEntity<Map<String, String>> generateCodingProblem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> params) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        String role = user.getTargetRole();
        if (role == null || role.isBlank()) {
            throw new IllegalStateException("Please select your target role before starting an interview.");
        }
        String topic = params != null ? params.get("topic") : null;
        String problem = interviewService.generateCodingProblem(role, topic);
        return ResponseEntity.ok(Map.of("problem", problem));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, String>> evaluateAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        String questionText = (String) body.get("questionText");
        String userAnswer = (String) body.get("userAnswer");
        Long sessionId = body.get("sessionId") instanceof Number n ? n.longValue() : null;
        String topic = (String) body.get("topic");
        String difficulty = (String) body.get("difficulty");
        if (questionText == null || userAnswer == null || sessionId == null) {
            return ResponseEntity.badRequest().build();
        }
        String feedback = interviewService.evaluateAndSaveAnswer(
                user.getId(), sessionId, questionText, userAnswer, topic, difficulty);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }

    @PostMapping("/evaluate-code")
    public ResponseEntity<Map<String, String>> evaluateCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        String code = (String) body.get("code");
        String problemStatement = (String) body.get("problemStatement");
        String executionOutput = (String) body.get("executionOutput");
        Long sessionId = body.get("sessionId") != null && body.get("sessionId") instanceof Number n
                ? n.longValue() : null;
        if (code == null) code = "";
        String feedback = interviewService.evaluateCodeAndSave(
                user.getId(), sessionId, problemStatement, code, executionOutput);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }

    @PostMapping("/session/{sessionId}/end")
    public ResponseEntity<Void> endSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        interviewService.endSession(sessionId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<InterviewSession> getSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        return interviewService.getSession(sessionId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewSession>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "20") int limit) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        List<InterviewSession> list = interviewService.getSessionHistory(user.getId(), role, limit);
        return ResponseEntity.ok(list);
    }
}
