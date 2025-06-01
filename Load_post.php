<?php
require_once 'db.php';
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');

// 출력 버퍼링 시작
ob_start();

// 게시글의 태그를 가져오는 함수
function getPostTags($mysqli, $postId) {
    $tags = [];
    $query = "SELECT t.name FROM tags t 
              JOIN post_tags pt ON t.tag_id = pt.tag_id 
              WHERE pt.post_id = ?";
    
    $stmt = $mysqli->prepare($query);
    if ($stmt) {
        $stmt->bind_param("i", $postId);
        if ($stmt->execute()) {
            $result = $stmt->get_result();
            while ($row = $result->fetch_assoc()) {
                $tags[] = $row['name'];
            }
        }
        $stmt->close();
    }
    return $tags;
}

try {
    $userId = $_GET['user_id'] ?? 0;

    // 게시글 목록 조회 (industries 테이블과 조인)
    $query = "SELECT p.*, 
        i.industry_name,
        (SELECT COUNT(*) FROM comments WHERE post_id = p.id) as commentCount,
        (SELECT COUNT(*) FROM likes WHERE post_id = p.id) as likeCount,
        (SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) as isLiked
        FROM posts p 
        LEFT JOIN industries i ON p.industry_id = i.industry_id
        ORDER BY p.id DESC";

    $stmt = $mysqli->prepare($query);
    if (!$stmt) {
        throw new Exception("Query preparation failed: " . $mysqli->error);
    }

    $stmt->bind_param("i", $userId);
    if (!$stmt->execute()) {
        throw new Exception("Query execution failed: " . $stmt->error);
    }

    $result = $stmt->get_result();
    if (!$result) {
        throw new Exception("Failed to get result: " . $stmt->error);
    }

    $posts = [];
    while ($row = $result->fetch_assoc()) {
        // industry_id가 null이면 "자유게시판", 아니면 해당 산업 분야 이름 사용
        $industry_id = $row['industry_id'];
        $industry_name = $industry_id ? $row['industry_name'] : "자유게시판";
        
        $posts[] = [
            'id' => (int)$row['id'],
            'title' => $row['title'],
            'content' => $row['content'],
            'category' => $industry_name,
            'industry_id' => $industry_id ? (int)$industry_id : null,
            'experience' => $row['experience'] ?? '',
            'time' => $row['created_at'],
            'commentCount' => (int)$row['commentCount'],
            'likeCount' => (int)$row['likeCount'],
            'isLiked' => (bool)($row['isLiked'] > 0),
            'user_id' => (int)$row['user_id'],
            'tags' => getPostTags($mysqli, $row['id'])
        ];
    }

    // 응답 데이터 구성
    $response = [
        "status" => "success",
        "data" => $posts
    ];

    // 디버깅을 위한 로그
    error_log("Response data before encoding: " . print_r($response, true));

    // JSON 인코딩 전 데이터 검증
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    if ($jsonResponse === false) {
        throw new Exception("JSON encoding error: " . json_last_error_msg());
    }

    // 디버깅을 위한 로그
    error_log("Final JSON response: " . $jsonResponse);

    // 출력 버퍼 정리
    ob_end_clean();
    
    // 최종 응답 전송
    echo $jsonResponse;

} catch (Exception $e) {
    error_log("Error in Load_post.php: " . $e->getMessage());
    // 출력 버퍼 정리
    ob_end_clean();
    
    // 에러 응답
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
} finally {
    // 리소스 정리
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($mysqli)) {
        $mysqli->close();
    }
}
?> 