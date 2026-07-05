package com.example.miniats.candidate.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.miniats.candidate.CandidateStatus;

import java.time.LocalDateTime;

@TableName("candidate_status_log")
public class CandidateStatusLog {
    private Long id;
    private Long candidateId;
    private CandidateStatus fromStatus;
    private CandidateStatus toStatus;
    private String note;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public CandidateStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(CandidateStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public CandidateStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(CandidateStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
