package com.example.miniats.interview.dto;

import com.example.miniats.interview.entity.InterviewRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InterviewResponse(
        Long id,
        Long candidateId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime interviewTime,
        String interviewer,
        BigDecimal score,
        String content,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        LocalDateTime updatedAt
) {
    public static InterviewResponse from(InterviewRecord record) {
        return new InterviewResponse(
                record.getId(),
                record.getCandidateId(),
                record.getInterviewTime(),
                record.getInterviewer(),
                record.getScore(),
                record.getContent(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
