package com.interviewprep.controller;

import com.interviewprep.config.RoleOptions;
import com.interviewprep.dto.RoleSelectionRequest;
import com.interviewprep.dto.UserDto;
import com.interviewprep.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final UserService userService;

    public RoleController(UserService userService) {
        this.userService = userService;
    }

    /** Predefined roles for dropdown (public, used on Role Selection page). */
    @GetMapping("/predefined")
    public ResponseEntity<List<String>> getPredefinedRoles() {
        return ResponseEntity.ok(RoleOptions.PREDEFINED_ROLES);
    }

    /** Set or update user's target role (after first login or from Profile). */
    @PostMapping("/select")
    public ResponseEntity<UserDto> selectRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RoleSelectionRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        var user = userService.findByEmail(userDetails.getUsername());
        UserDto dto = userService.updateRole(user.getId(), request.getRole());
        return ResponseEntity.ok(dto);
    }
}
