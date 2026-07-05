package com.example.miniats.candidate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.miniats.candidate.dto.CandidateDetailResponse;
import com.example.miniats.candidate.dto.CandidateRequest;
import com.example.miniats.candidate.dto.CandidateResponse;
import com.example.miniats.candidate.dto.StatusChangeRequest;
import com.example.miniats.candidate.dto.StatusLogResponse;
import com.example.miniats.candidate.entity.Candidate;
import com.example.miniats.candidate.entity.CandidateStatusLog;
import com.example.miniats.candidate.mapper.CandidateMapper;
import com.example.miniats.candidate.mapper.CandidateStatusLogMapper;
import com.example.miniats.common.BusinessException;
import com.example.miniats.common.PageResponse;
import com.example.miniats.interview.InterviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class CandidateService {

    private final CandidateMapper candidateMapper;
    private final CandidateStatusLogMapper statusLogMapper;
    private final InterviewService interviewService;
    private final Path resumeRoot;

    public CandidateService(CandidateMapper candidateMapper,
                            CandidateStatusLogMapper statusLogMapper,
                            InterviewService interviewService,
                            @Value("${app.upload.resume-dir:uploads/resumes}") String resumeDir) {
        this.candidateMapper = candidateMapper;
        this.statusLogMapper = statusLogMapper;
        this.interviewService = interviewService;
        this.resumeRoot = Path.of(resumeDir).toAbsolutePath().normalize();
    }

    public PageResponse<CandidateResponse> list(String keyword,
                                                String position,
                                                CandidateStatus status,
                                                long page,
                                                long pageSize,
                                                String sortBy,
                                                String sortOrder) {
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<Candidate>()
                .eq(Candidate::getDeleted, false)
                .eq(status != null, Candidate::getStatus, status)
                .eq(StringUtils.hasText(position), Candidate::getPosition, position)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Candidate::getName, keyword)
                        .or()
                        .like(Candidate::getPhone, keyword)
                        .or()
                        .like(Candidate::getPosition, keyword));
        applySort(wrapper, sortBy, sortOrder);
        Page<Candidate> requestPage = Page.of(normalizePage(page), normalizePageSize(pageSize));
        IPage<Candidate> result = candidateMapper.selectPage(requestPage, wrapper);
        return new PageResponse<>(
                result.getRecords().stream().map(CandidateResponse::from).toList(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        ensureValidPhone(request.phone());
        ensureNoDuplicateCandidate(request.name(), request.phone(), null);
        ensurePhoneAvailable(request.phone(), null);
        Candidate candidate = new Candidate();
        apply(candidate, request);
        candidate.setStatus(CandidateStatus.APPLIED);
        candidate.setDeleted(false);
        candidateMapper.insert(candidate);
        insertStatusLog(candidate.getId(), null, candidate.getStatus(), "创建候选人");
        return CandidateResponse.from(candidateMapper.selectById(candidate.getId()));
    }

    public CandidateDetailResponse detail(Long id) {
        Candidate candidate = requireCandidate(id);
        return new CandidateDetailResponse(
                CandidateResponse.from(candidate),
                interviewService.listByCandidate(id),
                statusLogs(id)
        );
    }

    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate candidate = requireCandidate(id);
        ensureValidPhone(request.phone());
        ensureNoDuplicateCandidate(request.name(), request.phone(), id);
        ensurePhoneAvailable(request.phone(), id);
        candidate.setName(request.name());
        candidate.setPhone(request.phone());
        candidate.setPosition(request.position());
        candidate.setWorkYears(request.workYears());
        candidate.setRemark(request.remark());
        candidateMapper.updateById(candidate);
        return CandidateResponse.from(requireCandidate(id));
    }

    @Transactional
    public CandidateResponse changeStatus(Long id, StatusChangeRequest request) {
        Candidate candidate = requireCandidate(id);
        CandidateStatus from = candidate.getStatus();
        CandidateStatus to = request.status();
        if (!CandidateStatusPolicy.canTransit(from, to)) {
            throw new BusinessException("状态流转不符合招聘流程");
        }
        candidate.setStatus(to);
        candidateMapper.updateById(candidate);
        insertStatusLog(id, from, to, request.note());
        return CandidateResponse.from(requireCandidate(id));
    }

    public List<StatusLogResponse> statusLogs(Long id) {
        return statusLogMapper.selectList(new LambdaQueryWrapper<CandidateStatusLog>()
                        .eq(CandidateStatusLog::getCandidateId, id)
                        .orderByDesc(CandidateStatusLog::getCreatedAt))
                .stream()
                .map(StatusLogResponse::from)
                .toList();
    }

    public void delete(Long id) {
        Candidate candidate = requireCandidate(id);
        candidate.setDeleted(true);
        candidateMapper.updateById(candidate);
    }

    @Transactional
    public CandidateResponse uploadResume(Long id, MultipartFile file) {
        Candidate candidate = requireCandidate(id);
        if (!CandidateResumePolicy.isValidPdf(file)) {
            throw new BusinessException("简历仅支持 PDF 文件");
        }
        try {
            Files.createDirectories(resumeRoot);
            deleteStoredResume(candidate);
            String storedName = id + "-" + UUID.randomUUID() + ".pdf";
            Path target = resumeRoot.resolve(storedName).normalize();
            if (!target.startsWith(resumeRoot)) {
                throw new BusinessException("简历文件路径不合法");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            candidate.setResumeOriginalName(file.getOriginalFilename());
            candidate.setResumeContentType(file.getContentType());
            candidate.setResumeSize(file.getSize());
            candidate.setResumePath(storedName);
            candidateMapper.updateById(candidate);
            return CandidateResponse.from(requireCandidate(id));
        } catch (IOException ex) {
            throw new BusinessException("简历上传失败");
        }
    }

    public CandidateResumeFile loadResume(Long id) {
        Candidate candidate = requireCandidate(id);
        if (!StringUtils.hasText(candidate.getResumePath())) {
            throw new BusinessException("该候选人暂无简历");
        }
        Path resumePath = resumeRoot.resolve(candidate.getResumePath()).normalize();
        if (!resumePath.startsWith(resumeRoot) || !Files.exists(resumePath)) {
            throw new BusinessException("简历文件不存在");
        }
        try {
            return new CandidateResumeFile(
                    new UrlResource(resumePath.toUri()),
                    candidate.getResumeOriginalName()
            );
        } catch (MalformedURLException ex) {
            throw new BusinessException("简历文件无法读取");
        }
    }

    private Candidate requireCandidate(Long id) {
        Candidate candidate = candidateMapper.selectById(id);
        if (candidate == null || Boolean.TRUE.equals(candidate.getDeleted())) {
            throw new BusinessException("候选人不存在");
        }
        return candidate;
    }

    private void ensureValidPhone(String phone) {
        if (!CandidatePhonePolicy.isValidMobile(phone)) {
            throw new BusinessException("联系方式必须是 11 位手机号");
        }
    }

    private void ensurePhoneAvailable(String phone, Long currentCandidateId) {
        Long count = candidateMapper.selectCount(new LambdaQueryWrapper<Candidate>()
                .eq(Candidate::getDeleted, false)
                .eq(Candidate::getPhone, phone)
                .ne(currentCandidateId != null, Candidate::getId, currentCandidateId));
        if (count != null && count > 0) {
            throw new BusinessException("该手机号已存在，请勿重复添加");
        }
    }

    private void ensureNoDuplicateCandidate(String name, String phone, Long currentCandidateId) {
        Long count = candidateMapper.selectCount(new LambdaQueryWrapper<Candidate>()
                .eq(Candidate::getDeleted, false)
                .eq(Candidate::getName, name)
                .eq(Candidate::getPhone, phone)
                .ne(currentCandidateId != null, Candidate::getId, currentCandidateId));
        if (count != null && count > 0) {
            throw new BusinessException("候选人姓名和手机号已存在，请勿重复添加");
        }
    }

    private long normalizePage(long page) {
        return Math.max(page, 1);
    }

    private long normalizePageSize(long pageSize) {
        if (pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private void applySort(LambdaQueryWrapper<Candidate> wrapper, String sortBy, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder) || "ascending".equalsIgnoreCase(sortOrder);
        String field = StringUtils.hasText(sortBy) ? sortBy : "updatedAt";
        switch (field) {
            case "name" -> wrapper.orderBy(true, asc, Candidate::getName);
            case "position" -> wrapper.orderBy(true, asc, Candidate::getPosition);
            case "status" -> wrapper.orderBy(true, asc, Candidate::getStatus);
            case "rating" -> wrapper.orderBy(true, asc, Candidate::getRating);
            case "createdAt" -> wrapper.orderBy(true, asc, Candidate::getCreatedAt);
            default -> wrapper.orderBy(true, asc, Candidate::getUpdatedAt);
        }
    }

    private void apply(Candidate candidate, CandidateRequest request) {
        candidate.setName(request.name());
        candidate.setPhone(request.phone());
        candidate.setPosition(request.position());
        candidate.setWorkYears(request.workYears());
        candidate.setStatus(request.status());
        candidate.setRemark(request.remark());
    }

    private void insertStatusLog(Long candidateId, CandidateStatus from, CandidateStatus to, String note) {
        CandidateStatusLog log = new CandidateStatusLog();
        log.setCandidateId(candidateId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setNote(note);
        statusLogMapper.insert(log);
    }

    private void deleteStoredResume(Candidate candidate) throws IOException {
        if (!StringUtils.hasText(candidate.getResumePath())) {
            return;
        }
        Path oldResume = resumeRoot.resolve(candidate.getResumePath()).normalize();
        if (oldResume.startsWith(resumeRoot)) {
            Files.deleteIfExists(oldResume);
        }
    }

    public record CandidateResumeFile(Resource resource, String originalName) {
    }
}
