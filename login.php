<?php
// ⚠️ 오류 출력 설정 (디버깅용 - 운영 시 제거 권장)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 로그 파일 설정
ini_set('log_errors', 1);
ini_set('error_log', 'login_errors.log');

// 출력 버퍼링 시작
ob_start();

// JSON 응답 헤더
header('Content-Type: application/json; charset=UTF-8');

// DB 연결
require_once 'db.php';

try {
    // JSON 요청 데이터 파싱
    $rawInput = file_get_contents("php://input");
    error_log("Received login request: " . $rawInput);

    $data = json_decode($rawInput);

    // 입력값 추출
    $phone = $data->phoneNumber ?? '';
    $password = $data->password ?? '';

    error_log("Login attempt - Phone: " . $phone . ", Password: " . $password . ", Length: " . strlen($password));
    error_log("Password bytes: " . bin2hex($password)); // 비밀번호의 바이트 값 확인

    // 유효성 체크
    if (empty($phone) || empty($password)) {
        error_log("Login failed - Missing phone or password");
        $response = [
            "status" => "error",
            "message" => "전화번호 또는 비밀번호가 누락되었습니다."
        ];
    } else {
        // 사용자 조회 쿼리 실행
        $stmt = $mysqli->prepare("SELECT user_id, password_hash, verification_status FROM SimpleUsers WHERE phonenumber = ?");
        $stmt->bind_param("s", $phone);
        $stmt->execute();
        $result = $stmt->get_result();

        // 사용자 존재 확인
        if ($result->num_rows === 0) {
            error_log("Login failed - User not found: " . $phone);
            $response = [
                "status" => "error",
                "message" => "등록되지 않은 전화번호입니다."
            ];
        } else {
            $row = $result->fetch_assoc();
            $storedHash = $row['password_hash'];
            $verificationStatus = $row['verification_status'];

            error_log("User found - ID: " . $row['user_id'] . ", Verification status: " . $verificationStatus);
            error_log("Stored hash: " . $storedHash);
            error_log("Input password: " . $password);

            // 인증 상태 확인
            if ($verificationStatus !== 'verified') {
                error_log("Login failed - Unverified user: " . $phone);
                $response = [
                    "status" => "error",
                    "message" => "전화번호 인증이 필요합니다."
                ];
            } else {
                // 비밀번호 검증
                $verifyResult = password_verify($password, $storedHash);
                error_log("Password verification result: " . ($verifyResult ? "true" : "false"));

                // 디버깅을 위한 추가 정보
                $testHash = password_hash($password, PASSWORD_DEFAULT);
                error_log("Test hash for input password: " . $testHash);
                error_log("Hash comparison: " . ($storedHash === $testHash ? "exact match" : "different"));

                // 해시 정보 분석
                error_log("Stored hash info:");
                error_log("- Length: " . strlen($storedHash));
                error_log("- Prefix: " . substr($storedHash, 0, 7));
                error_log("- Bytes: " . bin2hex($storedHash));
                error_log("Test hash info:");
                error_log("- Length: " . strlen($testHash));
                error_log("- Prefix: " . substr($testHash, 0, 7));
                error_log("- Bytes: " . bin2hex($testHash));

                // 비밀번호 직접 비교
                error_log("Direct password comparison:");
                error_log("- Stored password hash: " . $storedHash);
                error_log("- Input password hash: " . $testHash);
                error_log("- Are they equal? " . ($storedHash === $testHash ? "Yes" : "No"));

                if ($verifyResult) {
                    error_log("Login successful - User ID: " . $row['user_id']);
                    $response = [
                        "status" => "success",
                        "message" => "로그인 성공",
                        "user_id" => $row['user_id']
                    ];
                } else {
                    error_log("Login failed - Password mismatch for user: " . $phone);
                    error_log("Stored hash: " . $storedHash);
                    error_log("Generated hash for input: " . $testHash);
                    $response = [
                        "status" => "error",
                        "message" => "비밀번호가 일치하지 않습니다."
                    ];
                }
            }
        }
        $stmt->close();
    }

    // JSON 응답 생성
    $jsonResponse = json_encode($response);
    
    // 응답 길이 설정
    header('Content-Length: ' . strlen($jsonResponse));
    
    // 출력 버퍼 비우기
    ob_end_clean();
    
    // 응답 전송
    echo $jsonResponse;

} catch (Exception $e) {
    error_log("Login error: " . $e->getMessage());
    $errorResponse = [
        "status" => "error",
        "message" => "서버 오류가 발생했습니다."
    ];
    
    $jsonError = json_encode($errorResponse);
    header('Content-Length: ' . strlen($jsonError));
    ob_end_clean();
    echo $jsonError;
}

// 자원 정리
if (isset($mysqli)) {
    $mysqli->close();
}
?> 