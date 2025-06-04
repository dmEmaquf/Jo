<?php
// 출력 버퍼링 시작
ob_start();

// 오류 출력 설정
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// JSON 응답 형식 설정
header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// OPTIONS 요청 처리
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    ob_end_flush();
    exit();
}

// POST 요청이 아닌 경우 오류 반환
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        "status" => "error",
        "message" => "잘못된 요청 메소드입니다."
    ]);
    ob_end_flush();
    exit();
}

// JSON 요청 데이터 읽기
$json = file_get_contents("php://input");
$data = json_decode($json, true);

// JSON 파싱 오류 처리
if (json_last_error() !== JSON_ERROR_NONE) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "잘못된 JSON 형식입니다.",
        "debug" => [
            "json_error" => json_last_error_msg(),
            "raw_input" => $json
        ]
    ]);
    ob_end_flush();
    exit();
}

// 필수 필드 검증
if (!isset($data['phoneNumber']) || !isset($data['verificationCode'])) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "전화번호와 인증번호가 필요합니다."
    ]);
    ob_end_flush();
    exit();
}

// 전화번호 형식 검증
$phoneNumber = $data['phoneNumber'];
if (!preg_match('/^01[0-9]{8,9}$/', $phoneNumber)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "올바른 전화번호 형식이 아닙니다."
    ]);
    ob_end_flush();
    exit();
}

// 인증번호 형식 검증
$verificationCode = $data['verificationCode'];
if (!preg_match('/^[0-9]{6}$/', $verificationCode)) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => "올바른 인증번호 형식이 아닙니다."
    ]);
    ob_end_flush();
    exit();
}

// 데이터베이스 연결
require_once 'db.php';

try {
    // 인증번호 확인
    $stmt = $conn->prepare("SELECT * FROM sms_verifications WHERE phone_number = ? AND verification_code = ? AND expires_at > NOW() AND is_verified = 0 ORDER BY created_at DESC LIMIT 1");
    $stmt->bind_param("ss", $phoneNumber, $verificationCode);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        http_response_code(400);
        echo json_encode([
            "status" => "error",
            "message" => "인증번호가 일치하지 않거나 만료되었습니다."
        ]);
        ob_end_flush();
        exit();
    }

    // 인증 완료 처리
    $stmt = $conn->prepare("UPDATE sms_verifications SET is_verified = 1 WHERE phone_number = ? AND verification_code = ?");
    $stmt->bind_param("ss", $phoneNumber, $verificationCode);
    $stmt->execute();
    $stmt->close();

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "데이터베이스 오류가 발생했습니다.",
        "debug" => [
            "error" => $e->getMessage()
        ]
    ]);
    ob_end_flush();
    exit();
}

// 출력 버퍼 초기화
ob_clean();

// 성공 응답
echo json_encode([
    "status" => "success",
    "message" => "인증이 완료되었습니다.",
    "data" => [
        "phoneNumber" => $phoneNumber
    ]
]);

// 출력 버퍼 플러시
ob_end_flush(); 