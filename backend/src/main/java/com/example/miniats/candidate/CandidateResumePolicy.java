package com.example.miniats.candidate;

import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public final class CandidateResumePolicy {
    private CandidateResumePolicy() {
    }

    public static boolean isValidPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        return filename != null
                && filename.toLowerCase(Locale.ROOT).endsWith(".pdf")
                && "application/pdf".equalsIgnoreCase(contentType);
    }
}
