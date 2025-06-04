<?php
require_once __DIR__ . '/db.php';

try {
    // sms_verifications 테이블 생성
    $sql = "CREATE TABLE IF NOT EXISTS sms_verifications (
        id INT AUTO_INCREMENT PRIMARY KEY,
        phone_number VARCHAR(20) NOT NULL,
        verification_code VARCHAR(6) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        expires_at TIMESTAMP NOT NULL,
        is_verified BOOLEAN DEFAULT FALSE,
        INDEX idx_phone_number (phone_number),
        INDEX idx_verification_code (verification_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    if ($mysqli->query($sql)) {
        echo "테이블이 성공적으로 생성되었습니다.\n";
    } else {
        throw new Exception("테이블 생성 실패: " . $mysqli->error);
    }

} catch (Exception $e) {
    echo "오류 발생: " . $e->getMessage() . "\n";
} finally {
    if (isset($mysqli)) {
        $mysqli->close();
    }
} 