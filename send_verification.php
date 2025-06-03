<?php
// 모든 출력 버퍼링 시작
ob_start();

// 오류 로깅 설정
ini_set('log_errors', 1);
ini_set('error_log', 'php_errors.log');
error_reporting(E_ALL);

// 디버깅: 시작 로그
error_log("Script started");

// 모든 출력 버퍼 비우기
ob_clean();

// 헤더 설정
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// 디버깅: 요청 정보 로깅
$rawInput = file_get_contents("php://input");
error_log("Request received: " . $rawInput);

// 필수 파일 포함
require_once __DIR__ . '/db.php';

// Twilio SDK 사용 가능 여부 확인
$useTwilio = false;
if (file_exists(__DIR__ . '/vendor/autoload.php')) {
    require_once __DIR__ . '/vendor/autoload.php';
    $useTwilio = true;
}

// 디버깅: 파일 포함 확인
error_log("Required files included");

try {
    // JSON 요청 데이터 파싱
    $data = json_decode($rawInput);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception('잘못된 JSON 형식입니다: ' . json_last_error_msg());
    }
    
    // 전화번호 검증
    $phoneNumber = $data->phoneNumber ?? '';
    if (empty($phoneNumber)) {
        throw new Exception('전화번호가 필요합니다.');
    }

    // 디버깅: 전화번호 확인
    error_log("Phone number received: " . $phoneNumber);

    // 이전 인증번호 만료 처리
    $stmt = $mysqli->prepare("
        UPDATE sms_verifications 
        SET is_verified = TRUE 
        WHERE phone_number = ? AND expires_at < NOW()
    ");
    $stmt->bind_param("s", $phoneNumber);
    $stmt->execute();
    
    // 새로운 인증번호 생성 (6자리)
    $verificationCode = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
    $expiresAt = date('Y-m-d H:i:s', strtotime('+3 minutes'));
    
    // 디버깅: 인증번호 생성 확인
    error_log("Generated verification code: " . $verificationCode);
    
    // 인증번호 저장
    $stmt = $mysqli->prepare("
        INSERT INTO sms_verifications 
        (phone_number, verification_code, expires_at) 
        VALUES (?, ?, ?)
    ");
    $stmt->bind_param("sss", $phoneNumber, $verificationCode, $expiresAt);
    $stmt->execute();
    
    // 디버깅: DB 저장 확인
    error_log("Verification code saved to database");
    
    // SMS 발송 처리
    if ($useTwilio) {
        // Twilio를 사용한 SMS 발송
        $client = new Twilio\Rest\Client(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
        $message = $client->messages->create(
            $phoneNumber,
            [
                'from' => TWILIO_PHONE_NUMBER,
                'body' => "인증번호: $verificationCode"
            ]
        );
        error_log("SMS sent via Twilio");
    } else {
        // Twilio 없이 임시로 콘솔에 출력 (개발 환경에서만 사용)
        error_log("SMS would be sent to: $phoneNumber with code: $verificationCode");
        // TODO: 여기에 다른 SMS 서비스 연동 코드 추가 가능
    }
    
    // 모든 출력 버퍼 비우기
    ob_clean();
    
    // 성공 응답
    $response = [
        'status' => 'success',
        'message' => '인증번호가 발송되었습니다.'
    ];
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE);
    error_log("Sending response: " . $jsonResponse);
    
    // 응답 전송
    echo $jsonResponse;
    exit;

} catch (Exception $e) {
    // 디버깅: 예외 발생 로그
    error_log("Exception occurred: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    
    // 모든 출력 버퍼 비우기
    ob_clean();
    
    // 에러 응답
    $error = [
        'status' => 'error',
        'message' => $e->getMessage()
    ];
    $jsonError = json_encode($error, JSON_UNESCAPED_UNICODE);
    error_log("Sending error response: " . $jsonError);
    
    // 응답 전송
    http_response_code(500);
    echo $jsonError;
    exit;
} finally {
    // 리소스 정리
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($mysqli)) {
        $mysqli->close();
    }
    
    // 디버깅: 스크립트 종료 로그
    error_log("Script ended");
    
    // 출력 버퍼 종료
    ob_end_flush();
} 