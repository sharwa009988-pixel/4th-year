package com.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private String feedback;
    private Double score; // 0-10
    private String explanation;
    private String suggestions;
    private Boolean isCorrect;
    private String correctAnswer;
    private String reason;
}
