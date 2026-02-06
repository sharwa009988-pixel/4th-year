package com.interviewprep.service;

import com.interviewprep.dto.InterviewRequest;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.SessionQuestion;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.SessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Separate service for handling async question generation.
 * This is necessary because @Async methods must be called through a proxy,
 * which doesn't work when called from the same class instance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationService {

    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository questionRepository;
    private final AiService aiService;

    /**
     * Generate questions asynchronously in the background.
     * This method runs in a separate thread and transaction.
     */
    @Async("taskExecutor")
    @Transactional
    public void generateQuestionsAsync(Long sessionId, InterviewRequest request) {
        try {
            System.out.println("[ASYNC] Starting question generation for session " + sessionId);
            long startTime = System.currentTimeMillis();
            
            // Fetch the session in a new transaction context
            InterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));
            
            List<SessionQuestion> questions = generateQuestions(session, request);
            session.setQuestions(questions);
            sessionRepository.save(session);
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[ASYNC] ✓ Questions generated in " + duration + "ms. Count: " + questions.size());
            log.info("Generated {} questions for session {} in {}ms", questions.size(), sessionId, duration);
        } catch (Exception e) {
            System.err.println("[ASYNC-ERROR] Failed for session " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
            log.error("Error generating questions async for session {}", sessionId, e);
        }
    }

    private List<SessionQuestion> generateQuestions(InterviewSession session, InterviewRequest request) {
        List<SessionQuestion> questions = new ArrayList<>();
        int numberOfQuestions = request.getNumberOfQuestions() != null ? request.getNumberOfQuestions() : 5;
        String difficulty = request.getDifficulty() != null ? request.getDifficulty() : "MEDIUM";

        for (int i = 0; i < numberOfQuestions; i++) {
            SessionQuestion.QuestionType questionType = determineQuestionType(session.getType(), i);
            String questionText;

            SessionQuestion question = SessionQuestion.builder()
                    .session(session)
                    .type(questionType)
                    .questionOrder(i + 1)
                    .build();

            // For coding questions, generate problem details
            if (questionType == SessionQuestion.QuestionType.CODING) {
                String problemDetails = aiService.generateCodingProblem(
                        session.getUser().getRole(),
                        request.getTopic(),
                        difficulty
                );
                questionText = problemDetails;
                question.setTestCases(problemDetails);
            } else {
                questionText = aiService.generateQuestion(
                        session.getUser().getRole(),
                        request.getTopic(),
                        difficulty,
                        questionType.name(),
                        session.getType().name()
                );
            }

            question.setQuestionText(questionText);
            questions.add(question);
        }

        return questionRepository.saveAll(questions);
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
}
