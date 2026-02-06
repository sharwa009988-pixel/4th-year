package com.interviewprep.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One interview session: tied to a user and their role at the time.
 * Stores session type (MCQ, Subjective, Coding, Full Mock) and role for analytics.
 */
@Entity
@Table(name = "interview_sessions", indexes = {
    @Index(name = "idx_session_user", columnList = "user_id"),
    @Index(name = "idx_session_role", columnList = "role_snapshot")
})
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Role at time of session (for history/analytics). */
    @Column(name = "role_snapshot", nullable = false, length = 255)
    private String roleSnapshot;

    @Column(name = "session_type", nullable = false, length = 50)
    private String sessionType; // MCQ, SUBJECTIVE, CODING, FULL_MOCK

    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "difficulty", length = 20)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @Column(name = "time_per_question_sec")
    private Integer timeLimitPerQuestionSeconds;

    @Column(name = "time_overall_min")
    private Integer timeLimitOverallMinutes;

    @Column(name = "programming_language", length = 20)
    private String programmingLanguage; // JAVA, C, CPP, PYTHON

    @Column(name = "score")
    private Integer score;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionQuestion> questions = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRoleSnapshot() { return roleSnapshot; }
    public void setRoleSnapshot(String roleSnapshot) { this.roleSnapshot = roleSnapshot; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(Integer numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }

    public Integer getTimeLimitPerQuestionSeconds() { return timeLimitPerQuestionSeconds; }
    public void setTimeLimitPerQuestionSeconds(Integer timeLimitPerQuestionSeconds) { this.timeLimitPerQuestionSeconds = timeLimitPerQuestionSeconds; }

    public Integer getTimeLimitOverallMinutes() { return timeLimitOverallMinutes; }
    public void setTimeLimitOverallMinutes(Integer timeLimitOverallMinutes) { this.timeLimitOverallMinutes = timeLimitOverallMinutes; }

    public String getProgrammingLanguage() { return programmingLanguage; }
    public void setProgrammingLanguage(String programmingLanguage) { this.programmingLanguage = programmingLanguage; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public List<SessionQuestion> getQuestions() { return questions; }
    public void setQuestions(List<SessionQuestion> questions) { this.questions = questions; }
}
