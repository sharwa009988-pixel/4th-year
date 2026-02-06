package com.interviewprep.controller;

import com.interviewprep.entity.User;
import com.interviewprep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolesController {

    private final UserRepository userRepository;

    /**
     * Predefined professional roles for the role-selection UI.
     * "Other" allows the user to enter a free-text custom role.
     *
     * NOTE: This list is intentionally multi-domain (Java, frontend, data, DevOps, etc.).
     * The AI prompts use the selected value directly to tailor questions and feedback.
     */
    private static final List<String> PREDEFINED = List.of(
            "-- Choose a role --",

            // Java / Backend Focused
            "Java Full Stack Developer",
            "Java Backend Developer",
            "Spring Boot Microservices Engineer",
            "Java Enterprise Application Developer",
            "Senior Java Developer",
            "Spring Boot + Hibernate Specialist",

            // Frontend / Full Stack
            "React Frontend Developer",
            "Full Stack Developer (React + Node.js / MERN)",
            "Angular Full Stack Developer",

            // Other Popular Tech Roles
            "Python Backend Developer (Django / FastAPI)",
            "Data Scientist / Machine Learning Engineer",
            "DevOps Engineer",
            "Cloud Engineer (AWS / Azure / GCP)",
            "Software Engineer (General)",
            "Android Developer (Kotlin/Java)",
            "Flutter / Mobile Developer",
            "QA Automation Engineer / SDET",
            "Blockchain Developer",
            "Cybersecurity Analyst",

            // Custom
            "Other"
    );

    @GetMapping("/predefined")
    public ResponseEntity<List<String>> predefined() {
        return ResponseEntity.ok(PREDEFINED);
    }

    @PostMapping("/select")
    public ResponseEntity<?> select(@RequestBody Map<String, String> body, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required"));
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "User not found"));

        user.setRole(role);
        user.setRoleSelected(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Role selected", "role", role));
    }
}
