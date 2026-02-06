package com.interviewprep.controller;

import com.interviewprep.entity.User;
import com.interviewprep.repository.UserRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolesController {

    private final UserRepository userRepository;

    public RolesController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/predefined")
    public List<String> predefined() {
        return List.of(
                "Java Full Stack Developer",
                "Java Backend Developer",
                "Spring Boot Microservices Engineer",
                "Java Enterprise Application Developer",
                "Senior Java Developer",
                "Spring Boot + Hibernate Specialist",
                "React Frontend Developer",
                "Full Stack Developer (React + Node.js / MERN)",
                "Angular Full Stack Developer",
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
                "Other"
        );
    }

    @PostMapping("/select")
    public ResponseEntity<?> select(@RequestBody(required = true) java.util.Map<String, String> body, Authentication authentication) {
        String role = body.get("role");
        if (role == null || role.isBlank()) return ResponseEntity.badRequest().body("role_required");
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setTargetRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("email", user.getEmail(), "targetRole", user.getTargetRole()));
    }
}
