package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotNull(message = "Question ID is required")
    private Long questionId;

    private String answer;

    private String code; // For coding questions
    private String stdin; // For code execution
}
