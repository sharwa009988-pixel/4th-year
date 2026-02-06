package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body when user selects or changes their target role.
 */
public class RoleSelectionRequest {

    @NotBlank(message = "Role is required")
    @Size(max = 255)
    private String role;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
