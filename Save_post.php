<?php
// 모든 출력 버퍼링 시작
ob_start();

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
header('Content-Type: application/json; charset=UTF-8');

require_once 'db.php';

// 입력 데이터 로깅
$rawInput = file_get_contents("php://input");
error_log("Raw input: " . $rawInput);

$data = json_decode($rawInput);
if (json_last_error() !== JSON_ERROR_NONE) {
    ob_end_clean();
    echo json_encode([
        "status" => "error",
        "message" => "Invalid JSON input: " . json_last_error_msg()
    ]);
    exit;
}

$title = $data->title ?? '';
$content = $data->content ?? '';
$user_id = $data->user_id ?? 0;
$industry_id = isset($data->industry_id) ? $data->industry_id : null;
$tags = $data->tags ?? [];
$phone_number = $data->phone_number ?? '';
$experience = $data->experience ?? '';

// 로깅 추가
error_log("Processed data: " . json_encode([
    'title' => $title,
    'content' => $content,
    'user_id' => $user_id,
    'industry_id' => $industry_id,
    'tags' => $tags,
    'phone_number' => $phone_number,
    'experience' => $experience
]));

if (!$title || !$content || !$user_id) {
    ob_end_clean();
    echo json_encode([
        "status" => "error",
        "message" => "필수 입력값 누락"
    ]);
    exit;
}

try {
    $mysqli->begin_transaction();

    // 게시글 저장
    $stmt = $mysqli->prepare("INSERT INTO posts (title, content, user_id, industry_id) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssii", $title, $content, $user_id, $industry_id);

    if (!$stmt->execute()) {
        throw new Exception("게시글 저장 실패: " . $stmt->error);
    }

    $post_id = $mysqli->insert_id;

    // 태그 저장
    if (!empty($tags)) {
        foreach ($tags as $tagName) {
            // 1. 태그가 이미 존재하는지 확인
            $tagStmt = $mysqli->prepare("SELECT tag_id FROM tags WHERE name = ?");
            $tagStmt->bind_param("s", $tagName);
            $tagStmt->execute();
            $tagResult = $tagStmt->get_result();
            
            $tag_id = null;
            if ($tagRow = $tagResult->fetch_assoc()) {
                $tag_id = $tagRow['tag_id'];
            } else {
                // 2. 새로운 태그 생성
                $insertTagStmt = $mysqli->prepare("INSERT INTO tags (name) VALUES (?)");
                $insertTagStmt->bind_param("s", $tagName);
                if (!$insertTagStmt->execute()) {
                    throw new Exception("태그 생성 실패: " . $insertTagStmt->error);
                }
                $tag_id = $mysqli->insert_id;
                $insertTagStmt->close();
            }
            $tagStmt->close();

            // 3. post_tags 연결
            $postTagStmt = $mysqli->prepare("INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)");
            $postTagStmt->bind_param("ii", $post_id, $tag_id);
            if (!$postTagStmt->execute()) {
                throw new Exception("태그 연결 실패: " . $postTagStmt->error);
            }
            $postTagStmt->close();
        }
    }

    $mysqli->commit();
    
    // 출력 버퍼 정리 후 JSON 응답
    ob_end_clean();
    echo json_encode([
        "status" => "success",
        "message" => "게시글 저장 성공",
        "post_id" => $post_id
    ]);

} catch (Exception $e) {
    $mysqli->rollback();
    
    // 출력 버퍼 정리 후 에러 응답
    ob_end_clean();
    echo json_encode([
        "status" => "error",
        "message" => "저장 실패: " . $e->getMessage()
    ]);
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