<?php
// 출력 버퍼링 시작
ob_start();

// ⚠️ 개발 중 디버깅 설정 (배포 시 제거 권장)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 로그 파일 설정
ini_set('log_errors', 1);
ini_set('error_log', 'signup_errors.log');

// JSON 응답 헤더 설정
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// DB 연결
require_once "db.php";

try {
    // 요청 데이터 파싱
    $rawInput = file_get_contents("php://input");
    error_log("Received signup request: " . $rawInput);
    
    $data = json_decode($rawInput);
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("잘못된 JSON 형식입니다: " . json_last_error_msg());
    }

    // 입력값 추출
    $phone = $data->phoneNumber ?? "";
    $password = $data->password ?? "";
    $verificationCode = $data->verificationCode ?? "";

    error_log("Phone: " . $phone . ", Verification Code: " . $verificationCode);

    // 입력값 검증
    if (empty($phone) || empty($password) || empty($verificationCode)) {
        throw new Exception("전화번호, 비밀번호, 인증번호가 필요합니다.");
    }

    // 중복 체크: 이미 존재하는 전화번호인지 확인
    $checkStmt = $mysqli->prepare("SELECT user_id FROM SimpleUsers WHERE phonenumber = ?");
    $checkStmt->bind_param("s", $phone);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();

    if ($checkResult->num_rows > 0) {
        throw new Exception("이미 가입된 전화번호입니다. 로그인을 시도해주세요.");
    }
    $checkStmt->close();

    // 비밀번호 유효성 검사
    if (strlen($password) < 8) {
        throw new Exception("비밀번호는 8자 이상이어야 합니다.");
    }

    // 인증번호 확인 - 모든 조건을 개별적으로 확인
    $stmt = $mysqli->prepare("
        SELECT * FROM sms_verifications
        WHERE phone_number = ?
        ORDER BY created_at DESC
        LIMIT 1
    ");
    $stmt->bind_param("s", $phone);
    $stmt->execute();
    $result = $stmt->get_result();

    error_log("Verification check - Found rows: " . $result->num_rows);
    
    if ($result->num_rows === 0) {
        throw new Exception("인증번호를 먼저 발급받아주세요.");
    }

    $verification = $result->fetch_assoc();
    error_log("Latest verification code: " . $verification['verification_code']);
    error_log("Is verified: " . $verification['is_verified']);
    error_log("Expires at: " . $verification['expires_at']);
    error_log("Current time: " . date('Y-m-d H:i:s'));

    // 인증번호 일치 여부 확인
    if ($verification['verification_code'] !== $verificationCode) {
        throw new Exception("인증번호가 일치하지 않습니다.");
    }

    // 인증 상태 확인
    if ($verification['is_verified'] !== '1' && $verification['is_verified'] !== 1) {
        throw new Exception("인증번호가 아직 검증되지 않았습니다.");
    }

    // 만료 시간 확인
    if (strtotime($verification['expires_at']) < time()) {
        throw new Exception("인증번호가 만료되었습니다. 새로운 인증번호를 발급받아주세요.");
    }

    // 비밀번호 해싱
    $hashed = password_hash($password, PASSWORD_DEFAULT);

    // 신규 회원 저장
    $stmt = $mysqli->prepare("
        INSERT INTO SimpleUsers (
            phonenumber, 
            password_hash, 
            verification_status
        ) VALUES (?, ?, 'verified')
    ");
    $stmt->bind_param("ss", $phone, $hashed);

    if (!$stmt->execute()) {
        throw new Exception("회원가입 실패: " . $stmt->error);
    }

    $userId = $mysqli->insert_id;
    $response = [
        "status" => "success",
        "message" => "회원가입 성공",
        "userId" => $userId
    ];

} catch (Exception $e) {
    error_log("Signup error: " . $e->getMessage());
    http_response_code(400);
    $response = [
        "status" => "error",
        "message" => $e->getMessage()
    ];
} finally {
    // 연결 종료
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($mysqli)) {
        $mysqli->close();
    }
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
?> 