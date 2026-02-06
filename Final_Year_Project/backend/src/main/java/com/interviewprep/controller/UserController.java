package com.interviewprep.controller;

import com.interviewprep.entity.User;
import com.interviewprep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("email", user.getEmail());
        out.put("name", user.getName());
        out.put("userId", user.getId());
        out.put("role", user.getRole() == null ? "" : user.getRole());
        out.put("roleSelected", Boolean.TRUE.equals(user.getRoleSelected()));

        return ResponseEntity.ok(out);
    }
}
