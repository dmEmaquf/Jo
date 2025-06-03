<?php
require_once 'db.php';

$phone = '01067881413';
$newPassword = '1111';

// 현재 저장된 해시 확인
$stmt = $mysqli->prepare("SELECT password_hash FROM SimpleUsers WHERE phonenumber = ?");
$stmt->bind_param("s", $phone);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();
echo "Current hash: " . $row['password_hash'] . "\n";

// 새로운 해시 생성
$hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
echo "New hash: " . $hashedPassword . "\n";

// 해시 검증 테스트
$verifyResult = password_verify($newPassword, $hashedPassword);
echo "Verification test result: " . ($verifyResult ? "true" : "false") . "\n";

// 데이터베이스 업데이트
$stmt = $mysqli->prepare("UPDATE SimpleUsers SET password_hash = ? WHERE phonenumber = ?");
$stmt->bind_param("ss", $hashedPassword, $phone);

if ($stmt->execute()) {
    echo "Password reset successful.\n";
    
    // 업데이트된 해시 확인
    $stmt = $mysqli->prepare("SELECT password_hash FROM SimpleUsers WHERE phonenumber = ?");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();
    echo "Updated hash in database: " . $row['password_hash'] . "\n";
    
    // 최종 검증 테스트
    $verifyResult = password_verify($newPassword, $row['password_hash']);
    echo "Final verification test: " . ($verifyResult ? "true" : "false") . "\n";
} else {
    echo "Error resetting password: " . $stmt->error . "\n";
}

$stmt->close();
$mysqli->close();
?> 