package com.example.miniats.candidate.dto;

import com.example.miniats.candidate.CandidateStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull CandidateStatus status,
        String note
) {
}
