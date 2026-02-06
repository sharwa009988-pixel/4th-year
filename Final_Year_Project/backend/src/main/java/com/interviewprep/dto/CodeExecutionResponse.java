package com.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeExecutionResponse {
    private String output;
    private String error;
    private Integer statusCode;
    private String memory;
    private String cpuTime;
}
