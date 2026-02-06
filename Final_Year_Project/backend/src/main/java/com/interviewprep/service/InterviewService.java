package com.interviewprep.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewprep.dto.*;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.SessionQuestion;
import com.interviewprep.entity.User;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.SessionQuestionRepository;
import com.interviewprep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final QuestionGenerationService questionGenerationService;
    private final CodeExecutionService codeExecutionService;
    private final PdfExportService pdfExportService;
    private final ObjectMapper objectMapper;

    @Transactional
    public InterviewSession createSession(Long userId, InterviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        InterviewSession.InterviewType interviewType = toInterviewType(request.getType());
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .type(interviewType)
                .topic(request.getTopic())
                .roleSnapshot(user.getRole())
                .startTime(LocalDateTime.now())
                .build();

        session = sessionRepository.save(session);

        // Trigger async question generation (returns immediately so UI doesn't block)
        questionGenerationService.generateQuestionsAsync(session.getId(), request);

        log.info("Created interview session {} for user {} (question generation running async)", session.getId(), userId);
        return session;
    }

    private List<SessionQuestion> generateQuestions(InterviewSession session, InterviewRequest request) {
        List<SessionQuestion> questions = new ArrayList<>();
        int numberOfQuestions = request.getNumberOfQuestions() != null ? request.getNumberOfQuestions() : 5;
        String difficulty = request.getDifficulty() != null ? request.getDifficulty() : "MEDIUM";
        String role = session.getUser() != null ? session.getUser().getRole() : null;

        for (int i = 0; i < numberOfQuestions; i++) {
            SessionQuestion.QuestionType questionType = determineQuestionType(toInterviewType(request.getType()), i);
            String questionText;

            SessionQuestion question = SessionQuestion.builder()
                    .session(session)
                    .type(questionType)
                    .questionOrder(i + 1)
                    .build();

            // For coding questions, generate problem details
            if (questionType == SessionQuestion.QuestionType.CODING) {
                String problemDetails = aiService.generateCodingProblem(role, request.getTopic(), difficulty);
                questionText = (problemDetails != null && !problemDetails.isBlank()) ? problemDetails : "Solve the problem for topic: " + request.getTopic();
                question.setTestCases(problemDetails != null ? problemDetails : "");
            } else {
                questionText = aiService.generateQuestion(
                        role,
                        request.getTopic(),
                        difficulty,
                        questionType.name(),
                        toInterviewType(request.getType()).name()
                );
                if (questionText == null || questionText.isBlank()) {
                    questionText = "Explain key concepts for " + request.getTopic() + " (difficulty: " + difficulty + ").";
                }
            }

            question.setQuestionText(questionText);
            questions.add(question);
        }

        return questionRepository.saveAll(questions);
    }

    private static InterviewSession.InterviewType toInterviewType(String type) {
        if (type == null || type.isBlank()) {
            return InterviewSession.InterviewType.TECHNICAL;
        }
        try {
            return InterviewSession.InterviewType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return InterviewSession.InterviewType.TECHNICAL;
        }
    }

    private SessionQuestion.QuestionType determineQuestionType(InterviewSession.InterviewType interviewType, int index) {
        return switch (interviewType) {
            case TECHNICAL -> index % 2 == 0 ? SessionQuestion.QuestionType.MCQ : SessionQuestion.QuestionType.SUBJECTIVE;
            case CODING -> SessionQuestion.QuestionType.CODING;
            case BEHAVIORAL -> SessionQuestion.QuestionType.BEHAVIORAL;
            case FULL_MOCK -> {
                int mod = index % 4;
                yield switch (mod) {
                    case 0 -> SessionQuestion.QuestionType.MCQ;
                    case 1 -> SessionQuestion.QuestionType.SUBJECTIVE;
                    case 2 -> SessionQuestion.QuestionType.CODING;
                    default -> SessionQuestion.QuestionType.BEHAVIORAL;
                };
            }
        };
    }

    @Transactional
    public FeedbackResponse submitAnswer(Long userId, Long sessionId, AnswerRequest request) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to session");
        }

        SessionQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.getSession().getId().equals(sessionId)) {
            throw new RuntimeException("Question does not belong to this session");
        }

        question.setUserAnswer(request.getAnswer());

        // Handle coding questions differently
        String role = session.getUser() != null ? session.getUser().getRole() : null;

        if (question.getType() == SessionQuestion.QuestionType.CODING && request.getCode() != null) {
            // Execute code first
            CodeExecutionRequest execRequest = new CodeExecutionRequest();
            execRequest.setCode(request.getCode());
            execRequest.setStdin(request.getStdin());
            
            var execResponse = codeExecutionService.executeCode(execRequest);
            question.setCodeInput(request.getCode());
            question.setCodeOutput(execResponse.getOutput());

            // Evaluate coding solution
            String aiResponse = aiService.evaluateCodingSolution(
                    role,
                    question.getQuestionText(),
                    request.getCode(),
                    execResponse.getOutput(),
                    question.getTestCases()
            );
            parseAndSetFeedback(question, aiResponse);
        } else {
            if (request.getAnswer() == null || request.getAnswer().trim().isEmpty()) {
                throw new RuntimeException("Answer is required");
            }
            // Evaluate regular answer
            String aiResponse = aiService.evaluateAnswer(
                    role,
                    question.getQuestionText(),
                    request.getAnswer(),
                    question.getType().name(),
                    session.getTopic()
            );
            parseAndSetFeedback(question, aiResponse);
        }

        question = questionRepository.save(question);
        updateSessionScore(session);

        // Parse stored JSON into response fields (fallbacks if plain text)
        String feedback = extractFieldFromJson(question.getAiFeedback(), "feedback");
        if (feedback == null || feedback.isBlank()) {
            feedback = question.getAiFeedback();
        }

        return FeedbackResponse.builder()
                .feedback(feedback)
                .score(question.getScore())
                .explanation(extractFieldFromJson(question.getAiFeedback(), "explanation"))
                .suggestions(extractFieldFromJson(question.getAiFeedback(), "suggestions"))
                .isCorrect(extractBooleanFromJson(question.getAiFeedback(), "is_correct", question.getScore() != null && question.getScore() >= 7.0))
                .correctAnswer(extractFieldFromJson(question.getAiFeedback(), "correct_answer"))
                .reason(extractFieldFromJson(question.getAiFeedback(), "reason"))
                .build();
    }

    private void parseAndSetFeedback(SessionQuestion question, String aiResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            if (jsonNode.has("score")) {
                question.setScore(jsonNode.get("score").asDouble());
            }
            // Persist the full JSON so we can show explanation/suggestions later
            question.setAiFeedback(aiResponse);
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, storing as plain text: {}", e.getMessage());
            question.setAiFeedback(aiResponse);
            question.setScore(5.0); // Default score
        }
    }

    private String extractFieldFromJson(String jsonString, String field) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.has(field)) {
                return jsonNode.get(field).asText();
            }
        } catch (Exception e) {
            // Not JSON, return empty
        }
        return "";
    }

    private boolean extractBooleanFromJson(String jsonString, String field, boolean fallback) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.has(field)) {
                return jsonNode.get(field).asBoolean();
            }
        } catch (Exception e) {
            // ignore
        }
        return fallback;
    }

    private void updateSessionScore(InterviewSession session) {
        List<SessionQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrder(session.getId());
        double totalScore = questions.stream()
                .filter(q -> q.getScore() != null)
                .mapToDouble(SessionQuestion::getScore)
                .average()
                .orElse(0.0);
        session.setTotalScore(totalScore);
        sessionRepository.save(session);
    }

    public List<SessionSummaryResponse> getUserSessions(Long userId) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        return sessions.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public SessionSummaryResponse getSessionDetails(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return mapToSummary(session);
    }

    @Transactional
    public void endSession(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        session.setEndTime(LocalDateTime.now());
        updateSessionScore(session);
        sessionRepository.save(session);
    }

    private SessionSummaryResponse mapToSummary(InterviewSession session) {
        List<SessionQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrder(session.getId());
        
        List<SessionSummaryResponse.QuestionSummary> questionSummaries = questions.stream()
                .map(q -> SessionSummaryResponse.QuestionSummary.builder()
                        .questionId(q.getId())
                        .questionText(q.getQuestionText())
                        .score(q.getScore())
                        .type(q.getType().name())
                        .build())
                .collect(Collectors.toList());

        return SessionSummaryResponse.builder()
                .sessionId(session.getId())
                .type(session.getType())
                .topic(session.getTopic())
                .totalScore(session.getTotalScore())
                .startTime(session.getStartTime() != null ? session.getStartTime().toString() : null)
                .endTime(session.getEndTime() != null ? session.getEndTime().toString() : null)
                .questions(questionSummaries)
                .totalQuestions(questions.size())
                .answeredQuestions((int) questions.stream().filter(q -> q.getUserAnswer() != null).count())
                .build();
    }

    public byte[] exportSessionPdf(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        List<SessionQuestion> questions = questionRepository.findBySessionIdOrderByQuestionOrder(sessionId);
        SessionSummaryResponse summary = mapToSummary(session);
        
        return pdfExportService.generateSessionReport(session, questions, summary);
    }
}
