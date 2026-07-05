package com.example.miniats.candidate;

import com.example.miniats.candidate.dto.CandidateDetailResponse;
import com.example.miniats.candidate.dto.CandidateRequest;
import com.example.miniats.candidate.dto.CandidateResponse;
import com.example.miniats.candidate.dto.StatusChangeRequest;
import com.example.miniats.candidate.dto.StatusLogResponse;
import com.example.miniats.common.ApiResponse;
import com.example.miniats.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CandidateResponse>> list(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) String position,
                                                             @RequestParam(required = false) CandidateStatus status,
                                                             @RequestParam(defaultValue = "1") long page,
                                                             @RequestParam(defaultValue = "10") long pageSize,
                                                             @RequestParam(defaultValue = "updatedAt") String sortBy,
                                                             @RequestParam(defaultValue = "desc") String sortOrder) {
        return ApiResponse.ok(candidateService.list(keyword, position, status, page, pageSize, sortBy, sortOrder));
    }

    @PostMapping
    public ApiResponse<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
        return ApiResponse.ok(candidateService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CandidateDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(candidateService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CandidateResponse> update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return ApiResponse.ok(candidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/status")
    public ApiResponse<CandidateResponse> changeStatus(@PathVariable Long id,
                                                       @Valid @RequestBody StatusChangeRequest request) {
        return ApiResponse.ok(candidateService.changeStatus(id, request));
    }

    @PostMapping(value = "/{id}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CandidateResponse> uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(candidateService.uploadResume(id, file));
    }

    @GetMapping(value = "/{id}/resume", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> resume(@PathVariable Long id) {
        CandidateService.CandidateResumeFile resumeFile = candidateService.loadResume(id);
        String filename = resumeFile.originalName() == null ? "resume.pdf" : resumeFile.originalName();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resumeFile.resource());
    }

    @GetMapping("/{id}/status-logs")
    public ApiResponse<List<StatusLogResponse>> statusLogs(@PathVariable Long id) {
        return ApiResponse.ok(candidateService.statusLogs(id));
    }
}
