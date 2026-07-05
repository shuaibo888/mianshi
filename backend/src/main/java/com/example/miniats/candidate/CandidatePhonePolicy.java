package com.example.miniats.candidate;

public final class CandidatePhonePolicy {

    private static final String MOBILE_PATTERN = "^1\\d{10}$";

    private CandidatePhonePolicy() {
    }

    public static boolean isValidMobile(String phone) {
        return phone != null && phone.matches(MOBILE_PATTERN);
    }
}
