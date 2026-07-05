package com.example.miniats.interview.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InterviewRequest(
        @NotNull LocalDateTime interviewTime,
        @NotBlank String interviewer,
        @DecimalMin("0") @DecimalMax("5") BigDecimal score,
        @Size(max = 2000, message = "评价内容不能超过 2000 个字符") String content
) {
}
