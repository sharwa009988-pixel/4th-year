package com.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.interviewprep.entity.InterviewSession.InterviewType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewRequest {
    @NotBlank(message = "Interview type is required")
    @JsonAlias("mode")
    private String type;  // Accept as string, will be converted to enum

    private String topic;

    private String difficulty; // EASY, MEDIUM, HARD
    private Integer numberOfQuestions = 5;
    
    // Helper method to convert string to enum (null-safe)
    public InterviewType getTypeAsEnum() {
        if (this.type == null || this.type.isBlank()) {
            return InterviewType.TECHNICAL;
        }
        try {
            return InterviewType.valueOf(this.type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return InterviewType.TECHNICAL; // Default fallback
        }
    }
}
