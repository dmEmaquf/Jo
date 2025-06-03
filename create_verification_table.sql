CREATE TABLE IF NOT EXISTS verification_codes (
    phone VARCHAR(20) PRIMARY KEY,
    code VARCHAR(6) NOT NULL,
    created_at DATETIME NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    INDEX idx_phone (phone)
); 