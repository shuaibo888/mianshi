package com.example.miniats.interview;

import com.example.miniats.common.ApiResponse;
import com.example.miniats.interview.dto.InterviewRequest;
import com.example.miniats.interview.dto.InterviewResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/candidates/{candidateId}/interviews")
    public ApiResponse<List<InterviewResponse>> list(@PathVariable Long candidateId) {
        return ApiResponse.ok(interviewService.listByCandidate(candidateId));
    }

    @PostMapping("/candidates/{candidateId}/interviews")
    public ApiResponse<InterviewResponse> create(@PathVariable Long candidateId,
                                                 @Valid @RequestBody InterviewRequest request) {
        return ApiResponse.ok(interviewService.create(candidateId, request));
    }

    @PutMapping("/interviews/{id}")
    public ApiResponse<InterviewResponse> update(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        return ApiResponse.ok(interviewService.update(id, request));
    }

    @DeleteMapping("/interviews/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ApiResponse.ok();
    }
}
