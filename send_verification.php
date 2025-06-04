<?php
// 출력 버퍼링 시작
ob_start();

// ⚠️ 개발 중 디버깅 설정 (배포 시 제거 권장)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 실행 시간 제한 설정
set_time_limit(30);
ini_set('max_execution_time', 30);

// 로그 설정
ini_set('log_errors', 1);
ini_set('error_log', '/tmp/sms_errors.log');

// 요청 정보 로깅
error_log("Request Method: " . $_SERVER['REQUEST_METHOD']);
error_log("Request Headers: " . print_r(getallheaders(), true));
error_log("Raw Input: " . file_get_contents("php://input"));

// JSON 응답 헤더
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// 기본 응답 변수 초기화
$response = [
    "status" => "error",
    "message" => "알 수 없는 오류가 발생했습니다."
];

try {
    // OPTIONS 사전 요청 처리
    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        $response = [
            "status" => "success",
            "message" => "OK"
        ];
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        ob_end_flush();
        exit();
    }

    // POST 요청이 아닐 경우
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        http_response_code(405);
        $response = [
            "status" => "error",
            "message" => "잘못된 요청 메소드입니다. (현재 메소드: " . $_SERVER['REQUEST_METHOD'] . ")"
        ];
        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        ob_end_flush();
        exit();
    }

    // DB 연결
    require_once "db.php";

    $json = file_get_contents("php://input");
    $data = json_decode($json, true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("잘못된 JSON 형식입니다.");
    }

    if (!isset($data['phoneNumber'])) {
        throw new Exception("전화번호가 필요합니다.");
    }

    $phone = preg_replace('/[^0-9]/', '', $data['phoneNumber']);
    if (!preg_match('/^01[016789][0-9]{7,8}$/', $phone)) {
        throw new Exception("유효하지 않은 전화번호 형식입니다.");
    }

    $verificationCode = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);

    $stmt = $mysqli->prepare("
        INSERT INTO sms_verifications (
            phone_number, verification_code, is_verified,
            created_at, expires_at
        ) VALUES (?, ?, 0, NOW(), DATE_ADD(NOW(), INTERVAL 5 MINUTE))
    ");
    $stmt->bind_param("ss", $phone, $verificationCode);

    if (!$stmt->execute()) {
        throw new Exception("인증번호 저장 실패: " . $stmt->error);
    }

    $response = [
        "status" => "success",
        "message" => "인증번호가 전송되었습니다.",
        "data" => [
            "phoneNumber" => $phone,
            "verificationCode" => $verificationCode
        ]
    ];
} catch (Exception $e) {
    error_log("Error: " . $e->getMessage());
    http_response_code(400);
    $response = [
        "status" => "error",
        "message" => $e->getMessage(),
        "debug" => [
            "file" => $e->getFile(),
            "line" => $e->getLine(),
            "trace" => $e->getTraceAsString()
        ]
    ];
} finally {
    if (isset($stmt)) $stmt->close();
    if (isset($mysqli)) $mysqli->close();
}

// 출력 버퍼 비우기
ob_clean();

// JSON 응답 생성
$jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE);

// 응답 길이 설정
header('Content-Length: ' . strlen($jsonResponse));

// 응답 전송
echo $jsonResponse;

// 출력 버퍼 종료
ob_end_flush(); 