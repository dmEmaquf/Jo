<?php
// 출력 버퍼링 시작
ob_start();

// ⚠️ 개발 중 디버깅 설정 (배포 시 제거 권장)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 실행 시간 제한 설정
set_time_limit(30); // 30초
ini_set('max_execution_time', 30);

// 로그 파일 설정 - 시스템 로그 디렉토리 사용
ini_set('log_errors', 1);
ini_set('error_log', '/tmp/sms_errors.log');

// 요청 정보 로깅
error_log("Request Method: " . $_SERVER['REQUEST_METHOD']);
error_log("Request Headers: " . print_r(getallheaders(), true));
error_log("Raw Input: " . file_get_contents("php://input"));

// JSON 응답 헤더 설정
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
    // OPTIONS 요청 처리
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

    // POST 요청이 아닌 경우 오류 반환
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

    // JSON 요청 데이터 읽기
    $json = file_get_contents("php://input");
    $data = json_decode($json, true);

    // JSON 파싱 오류 처리
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("잘못된 JSON 형식입니다.");
    }

    // 전화번호 필드 검증
    if (!isset($data['phoneNumber'])) {
        throw new Exception("전화번호가 필요합니다.");
    }

    // 전화번호 형식 검증
    $phone = preg_replace('/[^0-9]/', '', $data['phoneNumber']);
    if (!preg_match('/^01[016789][0-9]{7,8}$/', $phone)) {
        throw new Exception('유효하지 않은 전화번호 형식입니다.');
    }

    // 6자리 랜덤 인증번호 생성
    $verificationCode = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);

    // 인증번호 저장
    $stmt = $mysqli->prepare("
        INSERT INTO sms_verifications (
            phone_number,
            verification_code,
            is_verified,
            created_at,
            expires_at
        ) VALUES (?, ?, 0, NOW(), DATE_ADD(NOW(), INTERVAL 5 MINUTE))
    ");
    $stmt->bind_param("ss", $phone, $verificationCode);
    
    if (!$stmt->execute()) {
        throw new Exception("인증번호 저장 실패: " . $stmt->error);
    }

    // Twilio 설정
    $account_sid = 'AC864c28e02855be96b986e7d2b48e93b7';
    $auth_token = 'bc4582bf70c4d675cb3d45a39b68c67a';
    $twilio_number = '+14155238886'; // 미국 Twilio 번호로 변경

    // 국제 형식으로 변환 (+82)
    $internationalPhone = '+82' . substr($phone, 1);

    try {
        // cURL 사용 가능 여부 확인
        if (!function_exists('curl_init')) {
            throw new Exception("cURL이 설치되어 있지 않습니다.");
        }

        // Twilio API 엔드포인트
        $url = "https://api.twilio.com/2010-04-01/Accounts/{$account_sid}/Messages.json";
        
        // 요청 데이터 준비
        $postData = http_build_query([
            'To' => $internationalPhone,
            'From' => $twilio_number,
            'Body' => "[GrowStudio] 인증번호: {$verificationCode}"
        ]);

        error_log("Twilio Request URL: " . $url);
        error_log("Twilio Request Data: " . $postData);

        // cURL 초기화
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_USERPWD, "{$account_sid}:{$auth_token}");
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/x-www-form-urlencoded'
        ]);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // SSL 인증서 검증 비활성화 (개발 환경에서만)
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false); // SSL 호스트 검증 비활성화 (개발 환경에서만)
        curl_setopt($ch, CURLOPT_TIMEOUT, 10); // 10초 타임아웃 설정
        curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5); // 5초 연결 타임아웃 설정

        // 요청 실행
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        $info = curl_getinfo($ch);
        
        error_log("Twilio Response Code: " . $httpCode);
        error_log("Twilio Response: " . $response);
        error_log("Twilio Error: " . $error);
        error_log("Twilio Info: " . print_r($info, true));
        
        curl_close($ch);

        if ($httpCode !== 201) {
            throw new Exception("SMS 전송 실패 (HTTP {$httpCode}): " . $error . "\nResponse: " . $response);
        }

        $response = [
            "status" => "success",
            "message" => "인증번호가 전송되었습니다.",
            "data" => [
                "phoneNumber" => $phone,
                "verificationCode" => $verificationCode // 개발 중에만 표시
            ]
        ];

    } catch (Exception $e) {
        throw new Exception("SMS 전송 실패: " . $e->getMessage());
    }

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