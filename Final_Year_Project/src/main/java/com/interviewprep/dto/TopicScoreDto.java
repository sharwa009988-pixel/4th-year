package com.interviewprep.dto;

public class TopicScoreDto {

    private String topic;
    private double averageScore;
    private long attemptCount;

    public TopicScoreDto() {}
    public TopicScoreDto(String topic, double averageScore, long attemptCount) {
        this.topic = topic;
        this.averageScore = averageScore;
        this.attemptCount = attemptCount;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    public long getAttemptCount() { return attemptCount; }
    public void setAttemptCount(long attemptCount) { this.attemptCount = attemptCount; }
}
