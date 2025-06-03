<?php
// ⚠️ 개발 중 디버깅 설정 (배포 시 제거 권장)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// JSON 응답 헤더 설정
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// DB 연결
require_once "db.php";

// 요청 데이터 파싱
$data = json_decode(file_get_contents("php://input"));

// 입력값 추출
$phone = $data->phoneNumber ?? "";
$password = $data->password ?? "";
$verificationCode = $data->verificationCode ?? "";

// 입력값 검증
if (empty($phone) || empty($password) || empty($verificationCode)) {
    echo json_encode([
        "status" => "error",
        "message" => "전화번호, 비밀번호, 인증번호가 필요합니다."
    ]);
    exit;
}

// 중복 체크: 이미 존재하는 전화번호인지 확인
$checkStmt = $mysqli->prepare("SELECT user_id FROM SimpleUsers WHERE phonenumber = ?");
$checkStmt->bind_param("s", $phone);
$checkStmt->execute();
$checkResult = $checkStmt->get_result();

if ($checkResult->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "이미 가입된 전화번호입니다. 로그인을 시도해주세요.",
        "errorCode" => "DUPLICATE_PHONE"
    ]);
    exit;
}
$checkStmt->close();

try {
    // 비밀번호 유효성 검사
    if (strlen($password) < 8) {
        throw new Exception("비밀번호는 8자 이상이어야 합니다.");
    }

    // 인증번호 확인
    $stmt = $mysqli->prepare("
        SELECT * FROM sms_verifications
        WHERE phone_number = ?
        AND verification_code = ?
        AND is_verified = TRUE
        AND expires_at > NOW()
        ORDER BY created_at DESC
        LIMIT 1
    ");
    $stmt->bind_param("ss", $phone, $verificationCode);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("유효하지 않은 인증번호입니다.");
    }

    // 비밀번호 해싱
    $hashed = password_hash($password, PASSWORD_DEFAULT);

    // 신규 회원 저장
    $stmt = $mysqli->prepare("
        INSERT INTO SimpleUsers (
            phonenumber, 
            password_hash, 
            verification_status,
            created_at
        ) VALUES (?, ?, 'verified', NOW())
    ");
    $stmt->bind_param("ss", $phone, $hashed);

    if ($stmt->execute()) {
        $userId = $mysqli->insert_id;
        echo json_encode([
            "status" => "success",
            "message" => "회원가입 성공",
            "userId" => $userId
        ]);
    } else {
        throw new Exception("회원가입 실패: " . $stmt->error);
    }

} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ]);
} finally {
    // 연결 종료
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($mysqli)) {
        $mysqli->close();
    }
} 