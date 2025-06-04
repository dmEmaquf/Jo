<?php
// 오류 출력 설정
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 데이터베이스 연결
require_once 'db.php';

// SQL 파일 읽기
$sql = file_get_contents('create_tables.sql');

// SQL 실행
if ($conn->multi_query($sql)) {
    echo "테이블이 성공적으로 생성되었습니다.\n";
} else {
    echo "테이블 생성 중 오류가 발생했습니다: " . $conn->error . "\n";
}

// 연결 종료
$conn->close(); 