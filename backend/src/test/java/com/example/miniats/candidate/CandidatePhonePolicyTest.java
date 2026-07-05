package com.example.miniats.candidate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CandidatePhonePolicyTest {

    @Test
    void acceptsElevenDigitMobileNumbers() {
        assertThat(CandidatePhonePolicy.isValidMobile("13800138000")).isTrue();
    }

    @Test
    void rejectsInvalidMobileNumbers() {
        assertThat(CandidatePhonePolicy.isValidMobile("23800138000")).isFalse();
        assertThat(CandidatePhonePolicy.isValidMobile("1380013800")).isFalse();
        assertThat(CandidatePhonePolicy.isValidMobile("138001380000")).isFalse();
        assertThat(CandidatePhonePolicy.isValidMobile("1380013800a")).isFalse();
        assertThat(CandidatePhonePolicy.isValidMobile(null)).isFalse();
    }
}
