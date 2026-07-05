package com.example.miniats.candidate;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateStatusPolicyTest {

    @Test
    void allowsForwardRecruitingWorkflow() {
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.APPLIED, CandidateStatus.SCREEN_PASSED)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.SCREEN_PASSED, CandidateStatus.INTERVIEWING)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.INTERVIEWING, CandidateStatus.OFFER_PENDING)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.OFFER_PENDING, CandidateStatus.HIRED)).isTrue();
    }

    @Test
    void allowsRejectingBeforeTerminalStates() {
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.APPLIED, CandidateStatus.REJECTED)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.SCREEN_PASSED, CandidateStatus.REJECTED)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.INTERVIEWING, CandidateStatus.REJECTED)).isTrue();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.OFFER_PENDING, CandidateStatus.REJECTED)).isTrue();
    }

    @Test
    void rejectsSkippingAndTerminalTransitions() {
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.APPLIED, CandidateStatus.HIRED)).isFalse();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.SCREEN_PASSED, CandidateStatus.OFFER_PENDING)).isFalse();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.HIRED, CandidateStatus.REJECTED)).isFalse();
        assertThat(CandidateStatusPolicy.canTransit(CandidateStatus.REJECTED, CandidateStatus.INTERVIEWING)).isFalse();
    }

    @Test
    void returnsAvailableNextStatuses() {
        assertThat(CandidateStatusPolicy.nextStatuses(CandidateStatus.APPLIED))
                .containsExactlyInAnyOrder(CandidateStatus.SCREEN_PASSED, CandidateStatus.REJECTED);
        assertThat(CandidateStatusPolicy.nextStatuses(CandidateStatus.HIRED)).isEmpty();
        assertThat(CandidateStatusPolicy.nextStatuses(CandidateStatus.REJECTED)).isEmpty();
    }

    @Test
    void interviewPassAdvancesThroughInterviewFlowOnlyBeforeTerminalStates() {
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.SCREEN_PASSED, BigDecimal.valueOf(4)))
                .containsExactly(CandidateStatus.INTERVIEWING, CandidateStatus.OFFER_PENDING);
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.INTERVIEWING, BigDecimal.valueOf(4.5)))
                .containsExactly(CandidateStatus.OFFER_PENDING);
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.HIRED, BigDecimal.valueOf(5))).isEmpty();
    }

    @Test
    void interviewWithoutPassScoreOnlyMarksScreenPassedCandidateAsInterviewing() {
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.SCREEN_PASSED, BigDecimal.valueOf(3.5)))
                .containsExactly(CandidateStatus.INTERVIEWING);
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.SCREEN_PASSED, null))
                .containsExactly(CandidateStatus.INTERVIEWING);
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.INTERVIEWING, BigDecimal.valueOf(3.5))).isEmpty();
        assertThat(CandidateStatusPolicy.transitionsAfterInterview(CandidateStatus.INTERVIEWING, null)).isEmpty();
    }
}
