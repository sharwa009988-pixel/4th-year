package com.interviewprep.service;

import com.interviewprep.dto.DashboardStatsDto;
import com.interviewprep.entity.InterviewSession;
import com.interviewprep.entity.User;
import com.interviewprep.repository.InterviewSessionRepository;
import com.interviewprep.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final InterviewSessionRepository sessionRepository;

    public DashboardService(UserRepository userRepository, InterviewSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public DashboardStatsDto getStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartTimeDesc(userId);

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTargetRole(user.getRole());
        dto.setTotalSessions(sessions.size());
        long sessionsThisRole = sessions.stream()
                .filter(s -> {
                    String rs = s.getRoleSnapshot();
                    String ur = user.getRole();
                    return rs != null && ur != null && rs.equalsIgnoreCase(ur);
                })
                .count();
        dto.setSessionsThisRole(sessionsThisRole);

        double avg = sessions.stream()
                .filter(s -> s.getTotalScore() != null)
                .mapToDouble(InterviewSession::getTotalScore)
                .average()
                .orElse(0.0);
        dto.setAverageScore(avg);

        List<String> recentTopics = sessions.stream()
                .map(InterviewSession::getTopic)
                .filter(Objects::nonNull)
                .filter(t -> !t.isBlank())
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
        dto.setRecentTopics(recentTopics);

        Map<String, List<String>> sw = new HashMap<>();
        List<String> strengths = sessions.stream()
                .filter(s -> s.getTotalScore() != null && s.getTotalScore() >= 7.0)
                .map(InterviewSession::getTopic)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<String> weaknesses = sessions.stream()
                .filter(s -> s.getTotalScore() != null && s.getTotalScore() < 5.0)
                .map(InterviewSession::getTopic)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        sw.put("strengths", strengths);
        sw.put("weaknesses", weaknesses);
        dto.setStrengthsWeaknesses(sw);

        Map<String, List<Double>> topicScoreMap = new HashMap<>();
        for (InterviewSession s : sessions) {
            if (s.getTopic() == null || s.getTopic().isBlank()) continue;
            if (s.getTotalScore() == null) continue;
            topicScoreMap.computeIfAbsent(s.getTopic(), k -> new ArrayList<>()).add(s.getTotalScore());
        }
        List<DashboardStatsDto.TopicScore> topicScores = topicScoreMap.entrySet().stream()
                .map(e -> {
                    double a = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    return new DashboardStatsDto.TopicScore(e.getKey(), a);
                })
                .sorted(Comparator.comparing(DashboardStatsDto.TopicScore::getTopic))
                .collect(Collectors.toList());
        dto.setTopicScores(topicScores);

        return dto;
    }
}
