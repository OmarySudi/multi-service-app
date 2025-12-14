package com.multiservice.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private int totalUsers;
    private int activeUsers;
    private int totalOrders;
    private double revenue;
    private LocalDateTime lastUpdated;
}
