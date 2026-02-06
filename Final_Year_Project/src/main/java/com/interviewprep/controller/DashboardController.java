package com.interviewprep.controller;

import com.interviewprep.dto.DashboardStatsDto;
import com.interviewprep.service.DashboardService;
import com.interviewprep.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        var user = userService.findByEmail(userDetails.getUsername());
        if (!user.hasRoleSelected()) {
            return ResponseEntity.badRequest().build();
        }
        DashboardStatsDto stats = dashboardService.getStats(user.getId());
        return ResponseEntity.ok(stats);
    }
}
