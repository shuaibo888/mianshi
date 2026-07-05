package com.example.miniats.candidate;

public enum CandidateStatus {
    APPLIED("已投递"),
    SCREEN_PASSED("初筛通过"),
    INTERVIEWING("面试中"),
    OFFER_PENDING("待 offer"),
    HIRED("已录用"),
    REJECTED("已淘汰");

    private final String label;

    CandidateStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
