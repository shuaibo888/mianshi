package com.example.miniats.options;

import com.example.miniats.candidate.CandidateStatus;
import com.example.miniats.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/options")
public class OptionController {

    @GetMapping
    public ApiResponse<Map<String, List<Map<String, String>>>> options() {
        List<Map<String, String>> statuses = Arrays.stream(CandidateStatus.values())
                .map(status -> Map.of("value", status.name(), "label", status.getLabel()))
                .toList();
        return ApiResponse.ok(Map.of("statuses", statuses));
    }
}
