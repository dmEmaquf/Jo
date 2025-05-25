<?php
require 'db.php';
header('Content-Type: application/json; charset=UTF-8');

// 디버깅을 위한 로그 추가
file_put_contents('like_debug.log', "=== Like Request Start ===\n", FILE_APPEND);

$data = json_decode(file_get_contents("php://input"), true);

$userId = $data['user_id'] ?? 0;
$postId = $data['post_id'] ?? 0;

// 요청 데이터 로깅
file_put_contents('like_debug.log', "user_id=$userId, post_id=$postId\n", FILE_APPEND);

if (!$userId || !$postId) {
    file_put_contents('like_debug.log', "Error: Missing required values\n", FILE_APPEND);
    echo json_encode(["status" => "error", "message" => "필수값 누락"]);
    exit;
}

// 좋아요 존재 여부 확인
$check = $conn->prepare("SELECT like_id FROM likes WHERE user_id = ? AND post_id = ?");
$check->bind_param("ii", $userId, $postId);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    // 이미 좋아요 → 취소
    $delete = $conn->prepare("DELETE FROM likes WHERE user_id = ? AND post_id = ?");
    $delete->bind_param("ii", $userId, $postId);
    $delete->execute();

    // posts 테이블 like_count 감소
    $update = $conn->prepare("UPDATE posts SET like_count = like_count - 1 WHERE id = ?");
    $update->bind_param("i", $postId);
    $update->execute();

    file_put_contents('like_debug.log', "Success: Like removed\n", FILE_APPEND);
    echo json_encode(["status" => "success", "liked" => false, "message" => "좋아요 취소"]);
} else {
    // 좋아요 추가
    $insert = $conn->prepare("INSERT INTO likes (user_id, post_id) VALUES (?, ?)");
    $insert->bind_param("ii", $userId, $postId);
    $insert->execute();

    // posts 테이블 like_count 증가
    $update = $conn->prepare("UPDATE posts SET like_count = like_count + 1 WHERE id = ?");
    $update->bind_param("i", $postId);
    $update->execute();

    file_put_contents('like_debug.log', "Success: Like added\n", FILE_APPEND);
    echo json_encode(["status" => "success", "liked" => true, "message" => "좋아요 추가"]);
}

// 리소스 정리
$check->close();
if (isset($delete)) $delete->close();
if (isset($insert)) $insert->close();
if (isset($update)) $update->close();
$conn->close();

file_put_contents('like_debug.log', "=== Like Request End ===\n\n", FILE_APPEND);
?> 