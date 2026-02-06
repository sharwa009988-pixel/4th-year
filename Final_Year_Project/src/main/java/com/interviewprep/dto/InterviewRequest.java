package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class InterviewRequest {

    /** MCQ, SUBJECTIVE, CODING, FULL_MOCK. Also accepts TECHNICAL, BEHAVIORAL for frontend compatibility. */
    @NotBlank
    @Pattern(regexp = "MCQ|SUBJECTIVE|CODING|FULL_MOCK|TECHNICAL|BEHAVIORAL")
    private String mode;

    /** Optional topic filter, e.g. "Spring Boot", "REST API" */
    private String topic;

    /** Optional difficulty: EASY, MEDIUM, HARD */
    private String difficulty;

    /** Number of questions for the interview (applies to MCQ/Subjective/Mixed). */
    @Min(1)
    @Max(30)
    private Integer numberOfQuestions;

    /** Per-question time limit in seconds (optional). */
    @Min(10)
    @Max(1800)
    private Integer timeLimitPerQuestionSeconds;

    /** Overall interview time limit in minutes (optional). */
    @Min(1)
    @Max(240)
    private Integer timeLimitOverallMinutes;

    /** Programming language for coding rounds: JAVA, C, CPP, PYTHON */
    @Pattern(regexp = "JAVA|C|CPP|PYTHON")
    private String programmingLanguage;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(Integer numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
    public Integer getTimeLimitPerQuestionSeconds() { return timeLimitPerQuestionSeconds; }
    public void setTimeLimitPerQuestionSeconds(Integer timeLimitPerQuestionSeconds) { this.timeLimitPerQuestionSeconds = timeLimitPerQuestionSeconds; }
    public Integer getTimeLimitOverallMinutes() { return timeLimitOverallMinutes; }
    public void setTimeLimitOverallMinutes(Integer timeLimitOverallMinutes) { this.timeLimitOverallMinutes = timeLimitOverallMinutes; }
    public String getProgrammingLanguage() { return programmingLanguage; }
    public void setProgrammingLanguage(String programmingLanguage) { this.programmingLanguage = programmingLanguage; }
}
