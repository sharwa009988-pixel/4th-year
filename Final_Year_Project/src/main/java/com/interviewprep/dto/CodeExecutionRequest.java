package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeExecutionRequest {

    @NotBlank
    private String code;

    /** Optional: problem statement for context in AI feedback */
    private String problemStatement;

    private Long sessionId;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getProblemStatement() { return problemStatement; }
    public void setProblemStatement(String problemStatement) { this.problemStatement = problemStatement; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
