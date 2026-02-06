package com.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InterviewStartResponse {
    private Long id;
    private String type;
    private String topic;
}

