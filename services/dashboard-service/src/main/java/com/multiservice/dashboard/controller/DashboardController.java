package com.multiservice.dashboard.controller;

import com.multiservice.common.constants.Permissions;
import com.multiservice.dashboard.dto.DashboardStats;
import com.multiservice.dashboard.dto.UserProfile;
import com.multiservice.dashboard.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.multiservice.common.constants.Permissions.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('" + DASHBOARD_STATS + "')")
    public ResponseEntity<DashboardStats> getStats(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('" + DASHBOARD_PROFILE + "')")
    public ResponseEntity<UserProfile> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(dashboardService.getUserProfile(email));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('" + DASHBOARD_ADMIN + "')")
    public ResponseEntity<String> adminOnly(Authentication authentication) {
        return ResponseEntity.ok("Admin access granted for: " + authentication.getName());
    }

    @GetMapping("/public/health")
    public ResponseEntity<String> health(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        log.debug("Dashboard Service is running: CorrelationId is {}",correlationId);
        if(correlationId != null)
            return ResponseEntity.ok("Dashboard Service is running! CorrelationId=" + correlationId);
        else
            return ResponseEntity.ok("Dashboard Service is running!");
    }
}
