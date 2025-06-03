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

try {
    // 트랜잭션 시작
    $mysqli->begin_transaction();

    // JSON 요청 데이터 파싱
    $data = json_decode($rawInput);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception('잘못된 JSON 형식입니다: ' . json_last_error_msg());
    }
    
    // 필수 데이터 검증
    $phoneNumber = $data->phoneNumber ?? '';
    $verificationCode = $data->verificationCode ?? '';
    
    if (empty($phoneNumber) || empty($verificationCode)) {
        throw new Exception('전화번호와 인증번호가 필요합니다.');
    }

    // 디버깅: 입력 데이터 확인
    error_log("Phone number: " . $phoneNumber);
    error_log("Verification code: " . $verificationCode);

    // 인증번호 확인
    $stmt = $mysqli->prepare("
        SELECT id, verification_code, expires_at, is_verified 
        FROM sms_verifications 
        WHERE phone_number = ? 
        AND verification_code = ?
        AND is_verified = FALSE 
        AND expires_at > NOW()
        ORDER BY created_at DESC 
        LIMIT 1
        FOR UPDATE
    ");
    $stmt->bind_param("ss", $phoneNumber, $verificationCode);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        throw new Exception('유효하지 않은 인증번호입니다.');
    }
    
    $verification = $result->fetch_assoc();
    
    // 현재 인증번호 완료 처리
    $stmt = $mysqli->prepare("
        UPDATE sms_verifications 
        SET is_verified = TRUE 
        WHERE id = ?
    ");
    $stmt->bind_param("i", $verification['id']);
    $stmt->execute();

    // 같은 전화번호의 다른 인증번호들 만료 처리
    $stmt = $mysqli->prepare("
        UPDATE sms_verifications 
        SET is_verified = TRUE 
        WHERE phone_number = ? 
        AND id != ? 
        AND is_verified = FALSE
    ");
    $stmt->bind_param("si", $phoneNumber, $verification['id']);
    $stmt->execute();

    // 트랜잭션 커밋
    $mysqli->commit();
    
    // 모든 출력 버퍼 비우기
    ob_clean();
    
    // 성공 응답
    $response = [
        'status' => 'success',
        'message' => '인증이 완료되었습니다.',
        'isVerified' => true
    ];
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE);
    error_log("Sending response: " . $jsonResponse);
    
    // 응답 전송
    echo $jsonResponse;
    exit;

} catch (Exception $e) {
    // 트랜잭션 롤백
    if (isset($mysqli)) {
        $mysqli->rollback();
    }

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
    http_response_code(400);
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