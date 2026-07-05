package com.example.miniats.candidate;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateResumePolicyTest {

    @Test
    void acceptsPdfFilesOnly() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes()
        );

        assertThat(CandidateResumePolicy.isValidPdf(pdf)).isTrue();
    }

    @Test
    void rejectsEmptyOrNonPdfFiles() {
        assertThat(CandidateResumePolicy.isValidPdf(null)).isFalse();
        assertThat(CandidateResumePolicy.isValidPdf(new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        ))).isFalse();
        assertThat(CandidateResumePolicy.isValidPdf(new MockMultipartFile(
                "file",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "content".getBytes()
        ))).isFalse();
    }
}
