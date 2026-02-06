package com.interviewprep.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column
    private Double score;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(name = "code_input", columnDefinition = "TEXT")
    private String codeInput;

    @Column(name = "code_output", columnDefinition = "TEXT")
    private String codeOutput;

    @Column(name = "test_cases", columnDefinition = "TEXT")
    private String testCases;

    public enum QuestionType {
        MCQ,
        SUBJECTIVE,
        CODING,
        BEHAVIORAL
    }
}
