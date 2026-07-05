package com.example.miniats.candidate.dto;

import com.example.miniats.interview.dto.InterviewResponse;

import java.util.List;

public record CandidateDetailResponse(
        CandidateResponse candidate,
        List<InterviewResponse> interviews,
        List<StatusLogResponse> statusLogs
) {
}
