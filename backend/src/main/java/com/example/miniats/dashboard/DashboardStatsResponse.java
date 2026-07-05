package com.example.miniats.dashboard;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardStatsResponse(
        long totalCandidates,
        Map<String, Long> statusCounts,
        BigDecimal averageRating,
        BigDecimal hireRate
) {
}
