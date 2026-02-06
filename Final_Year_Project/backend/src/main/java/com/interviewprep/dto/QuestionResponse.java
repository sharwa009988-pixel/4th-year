package com.interviewprep.dto;

import com.interviewprep.entity.SessionQuestion.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private QuestionType type;
    private Integer questionOrder;
    private String testCases; // For coding questions
    private String sampleInput;
    private String sampleOutput;
}
