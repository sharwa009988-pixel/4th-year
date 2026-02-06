package com.interviewprep.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity. Holds email, password (BCrypt), and the selected professional role.
 * Role must be set after first login before accessing dashboard.
 */
@Entity
@Table(name = "users", indexes = { @Index(name = "idx_user_email", columnList = "email") })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Size(min = 60, max = 60) // BCrypt hash length
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    /**
     * Professional role selected by user (e.g. "Java Full Stack Developer", or custom "Other" value).
     * Null until user completes Role Selection after first login.
     */
    @Column(name = "target_role", length = 255)
    private String targetRole;

    @Column(name = "role_selected_at")
    private Instant roleSelectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewSession> sessions = new ArrayList<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Instant getRoleSelectedAt() { return roleSelectedAt; }
    public void setRoleSelectedAt(Instant roleSelectedAt) { this.roleSelectedAt = roleSelectedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<InterviewSession> getSessions() { return sessions; }
    public void setSessions(List<InterviewSession> sessions) { this.sessions = sessions; }

    /** Returns true if user has completed role selection. */
    public boolean hasRoleSelected() {
        return targetRole != null && !targetRole.isBlank();
    }
}
