package com.interviewprep.service;

import com.interviewprep.dto.UserDto;
import com.interviewprep.entity.User;
import com.interviewprep.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * User and profile operations, including role selection/update.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Transactional
    public UserDto updateRole(Long userId, String role) {
        User user = findById(userId);
        String trimmed = role != null ? role.trim() : "";
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        user.setTargetRole(trimmed);
        user.setRoleSelectedAt(Instant.now());
        userRepository.save(user);
        return toDto(user);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setTargetRole(user.getTargetRole());
        dto.setRoleSelectedAt(user.getRoleSelectedAt());
        dto.setRoleSelected(user.hasRoleSelected());
        return dto;
    }
}
