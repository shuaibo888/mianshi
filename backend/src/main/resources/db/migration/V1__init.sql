CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS candidate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    phone VARCHAR(60) NOT NULL,
    position VARCHAR(100) NOT NULL,
    work_years DECIMAL(4,1) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    rating DECIMAL(3,1) NULL,
    remark VARCHAR(1000) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_candidate_status (status),
    INDEX idx_candidate_position (position),
    INDEX idx_candidate_updated_at (updated_at)
);

CREATE TABLE IF NOT EXISTS interview_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id BIGINT NOT NULL,
    interview_time DATETIME NOT NULL,
    interviewer VARCHAR(80) NOT NULL,
    score DECIMAL(3,1) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(id),
    INDEX idx_interview_candidate (candidate_id)
);

CREATE TABLE IF NOT EXISTS candidate_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    candidate_id BIGINT NOT NULL,
    from_status VARCHAR(30) NULL,
    to_status VARCHAR(30) NOT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_log_candidate (candidate_id)
);

INSERT INTO sys_user (username, password, display_name)
SELECT 'admin', '{noop}admin123456', '管理员'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');
