package com.interviewprep.service;

import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.SessionQuestion;
import com.interviewprep.entity.User;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.SessionQuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates interview sessions, records questions/answers, and delegates to AI for generation/evaluation.
 */
@Service
public class InterviewService {

    private static final Pattern SCORE_PATTERN = Pattern.compile("Score:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository questionRepository;
    private final AiInterviewService aiInterviewService;
    private final UserService userService;

    public InterviewService(InterviewSessionRepository sessionRepository,
                            SessionQuestionRepository questionRepository,
                            AiInterviewService aiInterviewService,
                            UserService userService) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.aiInterviewService = aiInterviewService;
        this.userService = userService;
    }

    @Transactional
    public InterviewSession startSession(
            Long userId,
            String mode,
            String topic,
            String difficulty,
            Integer numberOfQuestions,
            Integer timeLimitPerQuestionSeconds,
            Integer timeLimitOverallMinutes,
            String programmingLanguage) {
        User user = userService.findById(userId);
        if (!user.hasRoleSelected()) {
            throw new IllegalStateException("Please select your target role first.");
        }
        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setRoleSnapshot(user.getTargetRole());
        session.setSessionType(mode);
        session.setTopic(topic);
        session.setDifficulty(difficulty);
        session.setNumberOfQuestions(numberOfQuestions);
        session.setTimeLimitPerQuestionSeconds(timeLimitPerQuestionSeconds);
        session.setTimeLimitOverallMinutes(timeLimitOverallMinutes);
        session.setProgrammingLanguage(programmingLanguage);
        session.setStartedAt(Instant.now());
        return sessionRepository.save(session);
    }

    public String generateQuestion(String userRole, String mode, String topic, String difficulty) {
        return aiInterviewService.generateQuestion(userRole, mode, topic, difficulty);
    }

    public String generateCodingProblem(String userRole, String topic) {
        return aiInterviewService.generateCodingProblem(userRole, topic);
    }

    @Transactional
    public String evaluateAndSaveAnswer(Long userId, Long sessionId, String questionText, String userAnswer,
                                        String topic, String difficulty) {
        User user = userService.findById(userId);
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Session does not belong to user");
        }
        String feedback = aiInterviewService.evaluateAnswer(
                user.getTargetRole(), questionText, userAnswer, topic, difficulty);
        int score = parseScoreFromFeedback(feedback);

        SessionQuestion sq = new SessionQuestion();
        sq.setSession(session);
        sq.setQuestionText(questionText);
        sq.setUserAnswer(userAnswer);
        sq.setAiFeedback(feedback);
        sq.setScore(score);
        sq.setTopic(topic);
        sq.setDifficulty(difficulty != null ? difficulty : "MEDIUM");
        sq.setQuestionOrder(session.getQuestions().size());
        questionRepository.save(sq);
        session.getQuestions().add(sq);
        return feedback;
    }

    @Transactional
    public String evaluateCodeAndSave(Long userId, Long sessionId, String problemStatement, String code, String executionOutput) {
        User user = userService.findById(userId);
        String feedback = aiInterviewService.evaluateCode(user.getTargetRole(), problemStatement, code, executionOutput);
        int score = parseScoreFromFeedback(feedback);

        Optional<InterviewSession> opt = sessionRepository.findById(sessionId);
        if (opt.isPresent() && opt.get().getUser().getId().equals(userId)) {
            SessionQuestion sq = new SessionQuestion();
            sq.setSession(opt.get());
            sq.setQuestionText(problemStatement != null ? problemStatement : "Coding problem");
            sq.setUserAnswer(code);
            sq.setAiFeedback(feedback);
            sq.setScore(score);
            sq.setTopic("Coding");
            sq.setDifficulty("MEDIUM");
            sq.setQuestionOrder(opt.get().getQuestions().size());
            questionRepository.save(sq);
        }
        return feedback;
    }

    @Transactional
    public void endSession(Long sessionId, Long userId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.getUser().getId().equals(userId)) return;
        session.setEndedAt(Instant.now());
        int total = session.getQuestions().size();
        if (total > 0) {
            int sum = session.getQuestions().stream()
                    .mapToInt(sq -> sq.getScore() != null ? sq.getScore() : 0)
                    .sum();
            session.setScore(sum / total);
            session.setTotalQuestions(total);
        }
        sessionRepository.save(session);
    }

    public Optional<InterviewSession> getSession(Long sessionId, Long userId) {
        return sessionRepository.findById(sessionId)
                .filter(s -> s.getUser().getId().equals(userId));
    }

    public List<InterviewSession> getSessionHistory(Long userId, String roleFilter, int limit) {
        if (roleFilter != null && !roleFilter.isBlank()) {
            return sessionRepository.findByUserIdAndRoleOrderByStartedAtDesc(userId, roleFilter, PageRequest.of(0, limit));
        }
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, limit));
    }

    private int parseScoreFromFeedback(String feedback) {
        if (feedback == null) return 0;
        Matcher m = SCORE_PATTERN.matcher(feedback);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
