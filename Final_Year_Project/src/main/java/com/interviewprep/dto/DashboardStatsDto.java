package com.interviewprep.dto;

import java.util.List;
import java.util.Map;

/**
 * Dashboard payload: role, progress, topic coverage, strengths/weaknesses.
 */
public class DashboardStatsDto {

    private String targetRole;
    private long totalSessions;
    private long sessionsThisRole;
    private double averageScore;
    private List<TopicScoreDto> topicScores;
    private Map<String, Object> strengthsWeaknesses; // e.g. { "strengths": [...], "weaknesses": [...] }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
    public long getSessionsThisRole() { return sessionsThisRole; }
    public void setSessionsThisRole(long sessionsThisRole) { this.sessionsThisRole = sessionsThisRole; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    public List<TopicScoreDto> getTopicScores() { return topicScores; }
    public void setTopicScores(List<TopicScoreDto> topicScores) { this.topicScores = topicScores; }
    public Map<String, Object> getStrengthsWeaknesses() { return strengthsWeaknesses; }
    public void setStrengthsWeaknesses(Map<String, Object> strengthsWeaknesses) { this.strengthsWeaknesses = strengthsWeaknesses; }
}
