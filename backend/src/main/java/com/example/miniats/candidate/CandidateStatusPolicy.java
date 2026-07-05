package com.example.miniats.candidate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CandidateStatusPolicy {

    private static final Map<CandidateStatus, Set<CandidateStatus>> ALLOWED = Map.of(
            CandidateStatus.APPLIED, Set.of(CandidateStatus.SCREEN_PASSED, CandidateStatus.REJECTED),
            CandidateStatus.SCREEN_PASSED, Set.of(CandidateStatus.INTERVIEWING, CandidateStatus.REJECTED),
            CandidateStatus.INTERVIEWING, Set.of(CandidateStatus.OFFER_PENDING, CandidateStatus.REJECTED),
            CandidateStatus.OFFER_PENDING, Set.of(CandidateStatus.HIRED, CandidateStatus.REJECTED)
    );

    private CandidateStatusPolicy() {
    }

    public static boolean canTransit(CandidateStatus from, CandidateStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static Set<CandidateStatus> nextStatuses(CandidateStatus from) {
        if (from == null) {
            return Set.of();
        }
        return ALLOWED.getOrDefault(from, Set.of());
    }

    public static List<CandidateStatus> transitionsAfterInterview(CandidateStatus current, BigDecimal score) {
        List<CandidateStatus> transitions = new ArrayList<>();
        CandidateStatus status = current;
        if (status == CandidateStatus.SCREEN_PASSED) {
            transitions.add(CandidateStatus.INTERVIEWING);
            status = CandidateStatus.INTERVIEWING;
        }
        if (status == CandidateStatus.INTERVIEWING && isPassScore(score)) {
            transitions.add(CandidateStatus.OFFER_PENDING);
        }
        return transitions;
    }

    private static boolean isPassScore(BigDecimal score) {
        return score != null && score.compareTo(BigDecimal.valueOf(4)) >= 0;
    }
}
