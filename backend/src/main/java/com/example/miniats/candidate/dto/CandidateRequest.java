package com.example.miniats.candidate.dto;

import com.example.miniats.candidate.CandidateStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CandidateRequest(
        @NotBlank @Size(max = 80, message = "姓名不能超过 80 个字符") String name,
        @NotBlank @Pattern(regexp = "^1\\d{10}$", message = "联系方式必须是 11 位手机号") String phone,
        @NotBlank @Size(max = 100, message = "应聘岗位不能超过 100 个字符") String position,
        @NotNull @DecimalMin("0") @DecimalMax("60") BigDecimal workYears,
        @NotNull CandidateStatus status,
        @Size(max = 1000, message = "备注不能超过 1000 个字符") String remark
) {
}
