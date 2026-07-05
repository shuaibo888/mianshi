ALTER TABLE candidate
    ADD COLUMN resume_original_name VARCHAR(255) NULL AFTER remark,
    ADD COLUMN resume_content_type VARCHAR(100) NULL AFTER resume_original_name,
    ADD COLUMN resume_size BIGINT NULL AFTER resume_content_type,
    ADD COLUMN resume_path VARCHAR(500) NULL AFTER resume_size;
