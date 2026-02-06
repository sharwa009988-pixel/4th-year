package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeExecutionRequest {
    @NotBlank(message = "Code is required")
    private String code;
    private String stdin;
}
