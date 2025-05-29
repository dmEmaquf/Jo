<?php
// 에러 출력 설정 (개발용)
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// CORS 헤더 설정
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");
header("Access-Control-Max-Age: 86400");

// OPTIONS 요청 처리
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// 응답 형식 설정
header('Content-Type: application/json; charset=utf-8');

// DB 연결
require_once 'db.php';

// 데이터베이스 연결 확인
if (!isset($mysqli)) {
    echo json_encode([
        'success' => false,
        'error' => 'Database connection not established'
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// POST 요청 처리
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);
        
        // 로그 기록
        error_log("Received report request: " . json_encode($data));
        
        // 신고 접수 처리
        if (!isset($data['post_id']) || !isset($data['user_id']) || !isset($data['reason'])) {
            throw new Exception("필수 파라미터가 누락되었습니다.");
        }

        // 중복 신고 체크
        $checkStmt = $mysqli->prepare("SELECT report_id FROM reports WHERE post_id = ? AND user_id = ?");
        if (!$checkStmt) {
            throw new Exception("중복 신고 체크 쿼리 준비 실패: " . $mysqli->error);
        }
        
        $checkStmt->bind_param("ii", $data['post_id'], $data['user_id']);
        $checkStmt->execute();
        $result = $checkStmt->get_result();

        if ($result->num_rows > 0) {
            echo json_encode([
                'success' => false,
                'error' => '이미 신고한 게시글입니다.'
            ], JSON_UNESCAPED_UNICODE);
            exit();
        }

        // 신고 정보 저장
        $query = "INSERT INTO reports (post_id, user_id, reason, status, created_at) VALUES (?, ?, ?, 'PENDING', NOW())";
        $stmt = $mysqli->prepare($query);
        
        if (!$stmt) {
            throw new Exception("신고 저장 쿼리 준비 실패: " . $mysqli->error);
        }
        
        $stmt->bind_param("iis", $data['post_id'], $data['user_id'], $data['reason']);
        
        if ($stmt->execute()) {
            echo json_encode([
                'success' => true,
                'message' => '신고가 성공적으로 접수되었습니다.'
            ], JSON_UNESCAPED_UNICODE);
        } else {
            throw new Exception("신고 저장 실패: " . $stmt->error);
        }
        
        $stmt->close();
        
    } catch(Exception $e) {
        error_log("Error in report processing: " . $e->getMessage());
        echo json_encode([
            'success' => false,
            'error' => $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
    exit();
}

// GET 요청 처리
if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    try {
        $query = "SELECT r.*, p.title as post_title, p.content as post_content 
                 FROM reports r 
                 LEFT JOIN posts p ON r.post_id = p.id 
                 ORDER BY r.created_at DESC";
        $result = $mysqli->query($query);
        
        if (!$result) {
            throw new Exception("Query failed: " . $mysqli->error);
        }
        
        $reports = [];
        while ($row = $result->fetch_assoc()) {
            $reports[] = $row;
        }
        
        echo json_encode([
            'success' => true,
            'data' => $reports
        ], JSON_UNESCAPED_UNICODE);
        
    } catch(Exception $e) {
        error_log("Error in getting reports: " . $e->getMessage());
        echo json_encode([
            'success' => false,
            'error' => $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
    exit();
}

// PUT 요청 처리
if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);
        if (!isset($data['reportId']) || !isset($data['status'])) {
            throw new Exception("필수 파라미터가 누락되었습니다.");
        }
        
        $reportId = intval($data['reportId']);
        $status = $data['status'];
        
        $query = "UPDATE reports SET status = ? WHERE id = ?";
        $stmt = $mysqli->prepare($query);
        
        if (!$stmt) {
            throw new Exception("상태 업데이트 쿼리 준비 실패: " . $mysqli->error);
        }
        
        $stmt->bind_param("si", $status, $reportId);
        
        if ($stmt->execute()) {
            echo json_encode([
                'success' => true,
                'message' => '상태가 성공적으로 업데이트되었습니다.'
            ], JSON_UNESCAPED_UNICODE);
        } else {
            throw new Exception("상태 업데이트 실패: " . $stmt->error);
        }
        
        $stmt->close();
        
    } catch(Exception $e) {
        error_log("Error in updating report status: " . $e->getMessage());
        echo json_encode([
            'success' => false,
            'error' => $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
    exit();
}

// DELETE 요청 처리
if ($_SERVER['REQUEST_METHOD'] == 'DELETE') {
    try {
        $data = json_decode(file_get_contents('php://input'), true);
        if (!isset($data['post_id'])) {
            throw new Exception("게시글 ID가 누락되었습니다.");
        }
        
        $post_id = intval($data['post_id']);
        
        // 트랜잭션 시작
        $mysqli->begin_transaction();
        
        try {
            // 1. 해당 게시글의 신고 내역 삭제
            $query = "DELETE FROM reports WHERE post_id = ?";
            $stmt = $mysqli->prepare($query);
            if (!$stmt) {
                throw new Exception("신고 내역 삭제 쿼리 준비 실패: " . $mysqli->error);
            }
            $stmt->bind_param("i", $post_id);
            $stmt->execute();
            $stmt->close();
            
            // 2. 해당 게시글의 댓글 삭제
            $query = "DELETE FROM comments WHERE post_id = ?";
            $stmt = $mysqli->prepare($query);
            if (!$stmt) {
                throw new Exception("댓글 삭제 쿼리 준비 실패: " . $mysqli->error);
            }
            $stmt->bind_param("i", $post_id);
            $stmt->execute();
            $stmt->close();
            
            // 3. 게시글 삭제
            $query = "DELETE FROM posts WHERE id = ?";
            $stmt = $mysqli->prepare($query);
            if (!$stmt) {
                throw new Exception("게시글 삭제 쿼리 준비 실패: " . $mysqli->error);
            }
            $stmt->bind_param("i", $post_id);
            $stmt->execute();
            $stmt->close();
            
            // 트랜잭션 커밋
            $mysqli->commit();
            
            echo json_encode([
                'success' => true,
                'message' => '게시글이 성공적으로 삭제되었습니다.'
            ], JSON_UNESCAPED_UNICODE);
            
        } catch (Exception $e) {
            // 오류 발생 시 롤백
            $mysqli->rollback();
            throw $e;
        }
        
    } catch(Exception $e) {
        error_log("Error in deleting post: " . $e->getMessage());
        echo json_encode([
            'success' => false,
            'error' => $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
    exit();
}

// 리소스 정리
if (isset($checkStmt)) $checkStmt->close();
if (isset($stmt)) $stmt->close();
$mysqli->close();
?> 