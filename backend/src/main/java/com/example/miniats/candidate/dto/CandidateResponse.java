package com.example.miniats.candidate.dto;

import com.example.miniats.candidate.CandidateStatus;
import com.example.miniats.candidate.entity.Candidate;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CandidateResponse(
        Long id,
        String name,
        String phone,
        String position,
        BigDecimal workYears,
        CandidateStatus status,
        String statusLabel,
        BigDecimal rating,
        String remark,
        Boolean hasResume,
        String resumeOriginalName,
        Long resumeSize,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime updatedAt
) {
    public static CandidateResponse from(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getName(),
                candidate.getPhone(),
                candidate.getPosition(),
                candidate.getWorkYears(),
                candidate.getStatus(),
                candidate.getStatus().getLabel(),
                candidate.getRating(),
                candidate.getRemark(),
                candidate.getResumePath() != null,
                candidate.getResumeOriginalName(),
                candidate.getResumeSize(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }
}
