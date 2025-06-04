-- 인증번호 저장 테이블 생성
CREATE TABLE IF NOT EXISTS phone_verifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    verification_code VARCHAR(6) NOT NULL,
    created_at DATETIME NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at DATETIME NULL,
    INDEX idx_phone_number (phone_number),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 