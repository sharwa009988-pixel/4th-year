package com.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.interviewprep.entity.InterviewSession.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionSummaryResponse {
    private Long sessionId;
    private InterviewType type;
    private String topic;
    private Double totalScore;
    // Use simple strings for times to avoid frontend deserialization issues
    private String startTime;
    private String endTime;
    private List<QuestionSummary> questions;
    private Integer totalQuestions;
    private Integer answeredQuestions;

    @Data
    @Builder
    public static class QuestionSummary {
        private Long questionId;
        private String questionText;
        private Double score;
        private String type;
    }
}
