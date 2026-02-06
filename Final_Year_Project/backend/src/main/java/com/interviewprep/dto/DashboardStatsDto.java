package com.interviewprep.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    private String targetRole;
    private long totalSessions;
    private long sessionsThisRole;
    private double averageScore;
    private List<String> recentTopics;
    private Map<String, List<String>> strengthsWeaknesses;
    private List<TopicScore> topicScores;

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
    public long getSessionsThisRole() { return sessionsThisRole; }
    public void setSessionsThisRole(long sessionsThisRole) { this.sessionsThisRole = sessionsThisRole; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    public List<String> getRecentTopics() { return recentTopics; }
    public void setRecentTopics(List<String> recentTopics) { this.recentTopics = recentTopics; }
    public Map<String, List<String>> getStrengthsWeaknesses() { return strengthsWeaknesses; }
    public void setStrengthsWeaknesses(Map<String, List<String>> strengthsWeaknesses) { this.strengthsWeaknesses = strengthsWeaknesses; }
    public List<TopicScore> getTopicScores() { return topicScores; }
    public void setTopicScores(List<TopicScore> topicScores) { this.topicScores = topicScores; }

    public static class TopicScore {
        private String topic;
        private Double averageScore;

        public TopicScore() {}
        public TopicScore(String topic, Double averageScore) {
            this.topic = topic;
            this.averageScore = averageScore;
        }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    }
}
