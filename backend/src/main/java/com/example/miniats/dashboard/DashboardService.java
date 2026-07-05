package com.example.miniats.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.miniats.candidate.CandidateStatus;
import com.example.miniats.candidate.entity.Candidate;
import com.example.miniats.candidate.mapper.CandidateMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final CandidateMapper candidateMapper;

    public DashboardService(CandidateMapper candidateMapper) {
        this.candidateMapper = candidateMapper;
    }

    public DashboardStatsResponse stats() {
        List<Candidate> candidates = candidateMapper.selectList(new LambdaQueryWrapper<Candidate>()
                .eq(Candidate::getDeleted, false));
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        Arrays.stream(CandidateStatus.values()).forEach(status ->
                statusCounts.put(status.name(), candidates.stream().filter(c -> c.getStatus() == status).count()));

        BigDecimal averageRating = candidates.stream()
                .filter(c -> c.getRating() != null)
                .map(Candidate::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long ratedCount = candidates.stream().filter(c -> c.getRating() != null).count();
        if (ratedCount > 0) {
            averageRating = averageRating.divide(BigDecimal.valueOf(ratedCount), 2, RoundingMode.HALF_UP);
        }

        BigDecimal hireRate = candidates.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(statusCounts.get(CandidateStatus.HIRED.name()) * 100)
                .divide(BigDecimal.valueOf(candidates.size()), 2, RoundingMode.HALF_UP);

        return new DashboardStatsResponse(candidates.size(), statusCounts, averageRating, hireRate);
    }
}
