<?php
require 'db.php';

header('Content-Type: application/json; charset=UTF-8');
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// 디버깅을 위한 로그 파일 설정
error_log("=== Start of Get_post_by_id.php ===");

$postId = $_GET['post_id'] ?? 0;
$postId = intval($postId);

error_log("Requested post_id: " . $postId);

if (!$postId) {
    $response = [
        "status" => "error",
        "message" => "post_id가 필요합니다.",
        "data" => null
    ];
    error_log("Error response: " . json_encode($response));
    echo json_encode($response);
    exit;
}

try {
    // 게시글 조회
    $stmt = $conn->prepare("SELECT * FROM posts WHERE id = ?");
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("i", $postId);
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    error_log("SQL Query executed. Found rows: " . $result->num_rows);

    if ($row = $result->fetch_assoc()) {
        error_log("Raw post data: " . json_encode($row));
        
        // 댓글 개수 조회
        $commentCountResult = $conn->query("SELECT COUNT(*) as cnt FROM comments WHERE post_id = $postId");
        $commentCount = $commentCountResult->fetch_assoc()['cnt'];

        // 좋아요 개수 조회
        $likeCountResult = $conn->query("SELECT COUNT(*) as cnt FROM likes WHERE post_id = $postId");
        $likeCount = $likeCountResult->fetch_assoc()['cnt'];

        $post = [
            "id" => (int)$row['id'],
            "title" => $row['title'],
            "content" => $row['content'],
            "category" => $row['industry'],
            "experience" => $row['experience'] ?? "",
            "time" => $row['created_at'],
            "commentCount" => $commentCount,
            "likeCount" => (int)$likeCount,
            "isLiked" => false,
            "user_id" => (int)$row['user_id']
        ];

        error_log("Formatted post data: " . json_encode($post));

        $response = [
            "status" => "success",
            "message" => "게시글 조회 성공",
            "data" => $post
        ];

        error_log("Final response: " . json_encode($response));
        echo json_encode($response);
    } else {
        $response = [
            "status" => "error",
            "message" => "게시글을 찾을 수 없습니다.",
            "data" => null
        ];
        error_log("Not found response: " . json_encode($response));
        echo json_encode($response);
    }
} catch (Exception $e) {
    error_log("Error occurred: " . $e->getMessage());
    $response = [
        "status" => "error",
        "message" => "서버 오류가 발생했습니다: " . $e->getMessage(),
        "data" => null
    ];
    echo json_encode($response);
}

if (isset($stmt)) {
    $stmt->close();
}
$conn->close();

error_log("=== End of Get_post_by_id.php ===");
?> 