package com.example.miniats.candidate.dto;

import com.example.miniats.candidate.CandidateStatus;
import com.example.miniats.candidate.entity.CandidateStatusLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record StatusLogResponse(
        Long id,
        CandidateStatus fromStatus,
        String fromStatusLabel,
        CandidateStatus toStatus,
        String toStatusLabel,
        String note,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime createdAt
) {
    public static StatusLogResponse from(CandidateStatusLog log) {
        return new StatusLogResponse(
                log.getId(),
                log.getFromStatus(),
                log.getFromStatus() == null ? "创建" : log.getFromStatus().getLabel(),
                log.getToStatus(),
                log.getToStatus().getLabel(),
                log.getNote(),
                log.getCreatedAt()
        );
    }
}
