package com.example.miniats.interview;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.miniats.candidate.CandidateStatus;
import com.example.miniats.candidate.CandidateStatusPolicy;
import com.example.miniats.candidate.entity.Candidate;
import com.example.miniats.candidate.entity.CandidateStatusLog;
import com.example.miniats.candidate.mapper.CandidateMapper;
import com.example.miniats.candidate.mapper.CandidateStatusLogMapper;
import com.example.miniats.common.BusinessException;
import com.example.miniats.interview.dto.InterviewRequest;
import com.example.miniats.interview.dto.InterviewResponse;
import com.example.miniats.interview.entity.InterviewRecord;
import com.example.miniats.interview.mapper.InterviewRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRecordMapper interviewRecordMapper;
    private final CandidateMapper candidateMapper;
    private final CandidateStatusLogMapper statusLogMapper;

    public InterviewService(InterviewRecordMapper interviewRecordMapper,
                            CandidateMapper candidateMapper,
                            CandidateStatusLogMapper statusLogMapper) {
        this.interviewRecordMapper = interviewRecordMapper;
        this.candidateMapper = candidateMapper;
        this.statusLogMapper = statusLogMapper;
    }

    public List<InterviewResponse> listByCandidate(Long candidateId) {
        return interviewRecordMapper.selectList(new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getCandidateId, candidateId)
                        .orderByDesc(InterviewRecord::getInterviewTime))
                .stream()
                .map(InterviewResponse::from)
                .toList();
    }

    @Transactional
    public InterviewResponse create(Long candidateId, InterviewRequest request) {
        ensureCandidateExists(candidateId);
        InterviewRecord record = new InterviewRecord();
        record.setCandidateId(candidateId);
        apply(record, request);
        interviewRecordMapper.insert(record);
        refreshCandidateRating(candidateId);
        syncCandidateStatusAfterInterview(record);
        return InterviewResponse.from(record);
    }

    @Transactional
    public InterviewResponse update(Long id, InterviewRequest request) {
        InterviewRecord record = interviewRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("面试记录不存在");
        }
        apply(record, request);
        interviewRecordMapper.updateById(record);
        refreshCandidateRating(record.getCandidateId());
        syncCandidateStatusAfterInterview(record);
        return InterviewResponse.from(interviewRecordMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        InterviewRecord record = interviewRecordMapper.selectById(id);
        if (record == null || interviewRecordMapper.deleteById(id) == 0) {
            throw new BusinessException("面试记录不存在");
        }
        refreshCandidateRating(record.getCandidateId());
    }

    private void apply(InterviewRecord record, InterviewRequest request) {
        record.setInterviewTime(request.interviewTime());
        record.setInterviewer(request.interviewer());
        record.setScore(request.score());
        record.setContent(request.content());
    }

    private void ensureCandidateExists(Long candidateId) {
        if (candidateMapper.selectById(candidateId) == null) {
            throw new BusinessException("候选人不存在");
        }
    }

    private void syncCandidateStatusAfterInterview(InterviewRecord record) {
        Candidate candidate = candidateMapper.selectById(record.getCandidateId());
        if (candidate == null || Boolean.TRUE.equals(candidate.getDeleted())) {
            throw new BusinessException("候选人不存在");
        }
        CandidateStatus from = candidate.getStatus();
        for (CandidateStatus to : CandidateStatusPolicy.transitionsAfterInterview(from, record.getScore())) {
            candidate.setStatus(to);
            candidateMapper.updateById(candidate);
            insertStatusLog(candidate.getId(), from, to, interviewStatusNote(to, record));
            from = to;
        }
    }

    private void refreshCandidateRating(Long candidateId) {
        Candidate candidate = candidateMapper.selectById(candidateId);
        if (candidate == null || Boolean.TRUE.equals(candidate.getDeleted())) {
            throw new BusinessException("候选人不存在");
        }
        List<InterviewRecord> records = interviewRecordMapper.selectList(new LambdaQueryWrapper<InterviewRecord>()
                .eq(InterviewRecord::getCandidateId, candidateId))
                .stream()
                .filter(record -> record.getScore() != null)
                .toList();
        if (records.isEmpty()) {
            candidate.setRating(null);
        } else {
            BigDecimal totalScore = records.stream()
                    .map(InterviewRecord::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            candidate.setRating(totalScore.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP));
        }
        candidateMapper.updateById(candidate);
    }

    private void insertStatusLog(Long candidateId, CandidateStatus from, CandidateStatus to, String note) {
        CandidateStatusLog log = new CandidateStatusLog();
        log.setCandidateId(candidateId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setNote(note);
        statusLogMapper.insert(log);
    }

    private String interviewStatusNote(CandidateStatus to, InterviewRecord record) {
        if (to == CandidateStatus.INTERVIEWING) {
            return "添加面试记录，进入面试中";
        }
        if (to == CandidateStatus.OFFER_PENDING) {
            return "面试评分 " + record.getScore() + "，进入待 offer";
        }
        return "面试记录触发状态更新";
    }
}
