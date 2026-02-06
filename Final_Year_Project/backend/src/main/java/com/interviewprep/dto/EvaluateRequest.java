package com.interviewprep.dto;

import lombok.Data;

@Data
public class EvaluateRequest {
    private String questionText;
    private String userAnswer;
    private Long sessionId;
    private String topic;
    private String difficulty;
    private Long questionId;
}
