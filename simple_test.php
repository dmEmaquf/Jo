<?php
// 기본 설정
ini_set('display_errors', 1);
error_reporting(E_ALL);

// 헤더 설정
header('Content-Type: application/json; charset=UTF-8');

// 테스트 데이터
$test = [
    'status' => 'success',
    'message' => 'Simple test',
    'php_version' => PHP_VERSION,
    'server_software' => $_SERVER['SERVER_SOFTWARE'] ?? 'unknown',
    'request_method' => $_SERVER['REQUEST_METHOD'] ?? 'unknown',
    'content_type' => $_SERVER['CONTENT_TYPE'] ?? 'unknown'
];

// 응답 전송
echo json_encode($test, JSON_UNESCAPED_UNICODE);
?> 