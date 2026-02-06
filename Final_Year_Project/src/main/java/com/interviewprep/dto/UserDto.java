package com.interviewprep.dto;

import java.time.Instant;

/**
 * User response DTO. Includes role and roleSelected for frontend flow (e.g. redirect to Role Selection).
 */
public class UserDto {

    private Long id;
    private String email;
    private String targetRole;
    private Instant roleSelectedAt;
    private boolean roleSelected;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public Instant getRoleSelectedAt() { return roleSelectedAt; }
    public void setRoleSelectedAt(Instant roleSelectedAt) { this.roleSelectedAt = roleSelectedAt; }
    public boolean isRoleSelected() { return roleSelected; }
    public void setRoleSelected(boolean roleSelected) { this.roleSelected = roleSelected; }
}
