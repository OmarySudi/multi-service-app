package com.multiservice.dashboard.service;

import com.multiservice.dashboard.dto.DashboardStats;
import com.multiservice.dashboard.dto.UserProfile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardService {

    public DashboardStats getStats() {
        // Mock data - in real application, fetch from database
        return DashboardStats.builder()
                .totalUsers(1250)
                .activeUsers(845)
                .totalOrders(3420)
                .revenue(125430.50)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public UserProfile getUserProfile(String email) {
        // Mock data - in real application, fetch from database
        return UserProfile.builder()
                .email(email)
                .role("USER")
                .message("Profile fetched successfully")
                .build();
    }
}
