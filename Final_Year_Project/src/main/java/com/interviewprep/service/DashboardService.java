package com.interviewprep.service;

import com.interviewprep.dto.DashboardStatsDto;
import com.interviewprep.dto.TopicScoreDto;
import com.interviewprep.entity.User;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.SessionQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates dashboard stats: role, session counts, topic-wise scores, strengths/weaknesses.
 */
@Service
public class DashboardService {

    private final UserService userService;
    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository questionRepository;

    public DashboardService(UserService userService,
                            InterviewSessionRepository sessionRepository,
                            SessionQuestionRepository questionRepository) {
        this.userService = userService;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
    }

    public DashboardStatsDto getStats(Long userId) {
        User user = userService.findById(userId);
        if (!user.hasRoleSelected()) {
            throw new IllegalStateException("Please select your target role first.");
        }

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTargetRole(user.getTargetRole());
        long totalSessions = sessionRepository.countByUserId(userId);
        long sessionsThisRole = sessionRepository.countByUserIdAndRoleSnapshot(userId, user.getTargetRole());
        dto.setTotalSessions(totalSessions);
        dto.setSessionsThisRole(sessionsThisRole);

        List<Object[]> topicRows = questionRepository.findTopicWiseAverageScoreByUserId(userId);
        List<TopicScoreDto> topicScores = topicRows.stream()
                .map(row -> new TopicScoreDto(
                        (String) row[0],
                        ((Number) row[1]).doubleValue(),
                        0L))
                .collect(Collectors.toList());
        dto.setTopicScores(topicScores);

        double avg = topicScores.isEmpty() ? 0.0
                : topicScores.stream().mapToDouble(TopicScoreDto::getAverageScore).average().orElse(0.0);
        dto.setAverageScore(avg);

        Map<String, Object> sw = new HashMap<>();
        List<String> strengths = topicScores.stream()
                .filter(t -> t.getAverageScore() >= 70)
                .map(TopicScoreDto::getTopic)
                .collect(Collectors.toList());
        List<String> weaknesses = topicScores.stream()
                .filter(t -> t.getAverageScore() < 50)
                .map(TopicScoreDto::getTopic)
                .collect(Collectors.toList());
        sw.put("strengths", strengths);
        sw.put("weaknesses", weaknesses);
        dto.setStrengthsWeaknesses(sw);

        return dto;
    }
}
