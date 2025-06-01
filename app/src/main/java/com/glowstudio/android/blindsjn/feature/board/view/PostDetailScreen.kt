package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.feature.board.model.Comment
import com.glowstudio.android.blindsjn.feature.board.model.Post
import com.glowstudio.android.blindsjn.feature.board.viewmodel.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, postId: String) {
    val context = LocalContext.current
    val postViewModel: PostViewModel = viewModel()
    val commentViewModel: CommentViewModel = viewModel()
    val post by postViewModel.selectedPost.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val statusMessage by postViewModel.statusMessage.collectAsState(initial = "")
    val commentStatusMessage by commentViewModel.statusMessage.collectAsState(initial = "")
    var newComment by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(post?.isLiked ?: false) }
    var likeCount by remember { mutableIntStateOf(post?.likeCount ?: 0) }
    var isLiking by remember { mutableStateOf(false) }
    
    // 신고 관련 상태를 상위에서 관리
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    val reportResult by postViewModel.reportResult.collectAsState()

    val safePostId = postId.toIntOrNull() ?: 1
    val userId by UserManager.getUserId(context).collectAsState(initial = -1)

    LaunchedEffect(safePostId) {
        postViewModel.loadPostById(safePostId)
        commentViewModel.loadComments(safePostId)
    }

    LaunchedEffect(post) {
        post?.let {
            isLiked = it.isLiked
            likeCount = it.likeCount
        }
    }

    PostDetailScreenContent(
        post = post,
        comments = comments,
        newComment = newComment,
        onCommentChange = { newComment = it },
        onCommentSubmit = {
            if (newComment.isNotBlank() && userId > 0) {
                commentViewModel.saveComment(
                    postId = safePostId,
                    userId = userId,
                    content = newComment
                )
                newComment = ""
            }
        },
        isLiked = isLiked,
        onLikeClick = {
            if (!isLiking && userId > 0) {
                isLiking = true
                android.util.Log.d("PostDetailScreen", "Sending like request for postId: $safePostId")
                postViewModel.toggleLike(safePostId, userId) { success, newIsLiked, newLikeCount ->
                    android.util.Log.d("PostDetailScreen", "Like response - success: $success, newIsLiked: $newIsLiked, newLikeCount: $newLikeCount")
                    if (success) {
                        isLiked = newIsLiked
                        likeCount = newLikeCount
                    }
                    isLiking = false
                }
            } else {
                android.util.Log.d("PostDetailScreen", "Like request skipped - isLiking: $isLiking, userId: $userId")
            }
        },
        showReportDialog = showReportDialog,
        onShowReportDialogChange = { showReportDialog = it },
        reportReason = reportReason,
        onReportReasonChange = { reportReason = it },
        onReportSubmit = {
            if (userId > 0) {
                postViewModel.reportPost(postId = safePostId, userId = userId, reason = reportReason)
                showReportDialog = false
            }
        },
        likeCount = likeCount,
        isLiking = isLiking,
        userId = userId,
        modifier = Modifier
    )

    // 신고 결과 알림
    reportResult?.let {
        AlertDialog(
            onDismissRequest = { postViewModel.clearReportResult() },
            title = { Text("알림") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { postViewModel.clearReportResult() }) { Text("확인") }
            }
        )
    }
}

@Composable
fun PostDetailScreenContent(
    post: Post?,
    comments: List<Comment>,
    newComment: String,
    onCommentChange: (String) -> Unit,
    onCommentSubmit: () -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    showReportDialog: Boolean,
    onShowReportDialogChange: (Boolean) -> Unit,
    reportReason: String,
    onReportReasonChange: (String) -> Unit,
    onReportSubmit: () -> Unit,
    likeCount: Int,
    isLiking: Boolean,
    userId: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 스크롤 가능한 게시글과 댓글 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            post?.let {
                Spacer(modifier = Modifier.height(8.dp))
                // 작성자 정보
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("익명", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            it.time,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = onLikeClick,
                        enabled = !isLiking && userId != null,
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ThumbUp,
                                contentDescription = null,
                                tint = if (isLiked) Error else DividerGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                likeCount.toString(),
                                color = if (isLiked) Error else DividerGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = it.title ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it.content ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = Int.MAX_VALUE
                )
                
                // 해시태그 표시
                if (it.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var currentRow = mutableListOf<String>()
                        var currentRowWidth = 0
                        val maxWidth = 300 // 예상 최대 너비
                        
                        it.tags.forEach { tag ->
                            val tagWidth = tag.length * 8 + 24 // 대략적인 태그 너비 계산
                            if (currentRowWidth + tagWidth > maxWidth) {
                                // 현재 행이 가득 차면 새로운 행 시작
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    currentRow.forEach { rowTag ->
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = Color.Gray.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "#$rowTag",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                currentRow.clear()
                                currentRowWidth = 0
                            }
                            currentRow.add(tag)
                            currentRowWidth += tagWidth
                        }
                        
                        // 마지막 행 표시
                        if (currentRow.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                currentRow.forEach { tag ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = Color.Gray.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // 좋아요/댓글 숫자

                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    color = DividerGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "좋아요",
                        tint = if (isLiked) Error else DividerGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        it.likeCount.toString(),
                        color = if (isLiked) Error else DividerGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "댓글",
                        tint = ChatBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        it.commentCount.toString(),
                        color = ChatBlue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { onShowReportDialogChange(true) }) {
                        Icon(
                            imageVector = Icons.Filled.Report,
                            contentDescription = "신고",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 댓글 목록
            Spacer(modifier = Modifier.height(16.dp))
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 고정된 댓글 입력창
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (newComment.isEmpty()) {
                    Text(
                        text = "댓글을 입력하세요.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                BasicTextField(
                    value = newComment,
                    onValueChange = { onCommentChange(it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(
                onClick = {
                    if (newComment.isNotBlank()) onCommentSubmit()
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "댓글 등록",
                    tint = ChatBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { onShowReportDialogChange(false) },
                title = { Text("게시글 신고") },
                text = {
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = onReportReasonChange,
                        placeholder = { Text("신고 사유를 입력하세요") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onReportSubmit
                    ) { Text("신고하기") }
                },
                dismissButton = {
                    TextButton(onClick = { onShowReportDialogChange(false) }) { Text("취소") }
                }
            )
        }
    }
}

@Composable
fun PostDetailCard(
    post: Post,
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 상단 프로필/날짜
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = DividerGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("익명", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        post.time,
                        color = DividerGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 본문
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))
            // 좋아요/댓글 숫자
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    post.likeCount.toString(),
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = ChatBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    post.commentCount.toString(),
                    color = ChatBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 버튼
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = CardWhite,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = DividerGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("공감", color = DividerGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = DividerGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "익명",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = DividerGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = DividerGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = DividerGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.content ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.createdAt ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = DividerGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostDetailScreenPreview() {
    val navController = rememberNavController()
    val dummyPost = Post(
        id = 1,
        title = "다들 유심 바꿨나요?",
        content = "저도 가게 하루 쉬고 바꾸러 가야할 거 같은데 시간이 없네요..",
        category = "잡담",
        time = "2025-05-15 02:05:34",
        commentCount = 1,
        likeCount = 1
    )
    val dummyComments = listOf(
        Comment(
            commentId = 1,
            postId = 1,
            userId = 1,
            content = "어제 대리점 갔다가 없다고 해서 그냥 돌아왔네요..",
            createdAt = "2025-05-15 16:09:50"
        )
    )
    var previewComment by remember { mutableStateOf("") }
    var previewLiked by remember { mutableStateOf(false) }
    PostDetailScreenContent(
        post = dummyPost,
        comments = dummyComments,
        newComment = previewComment,
        onCommentChange = { previewComment = it },
        onCommentSubmit = {
            previewComment = ""
        },
        isLiked = previewLiked,
        onLikeClick = { previewLiked = !previewLiked },
        showReportDialog = false,
        onShowReportDialogChange = { },
        reportReason = "",
        onReportReasonChange = { },
        onReportSubmit = { },
        likeCount = 1,
        isLiking = false,
        userId = 1
    )
}

@Preview(showBackground = true)
@Composable
fun CommentItemPreview() {
    BlindSJNTheme {
        CommentItem(
            comment = Comment(
                commentId = 1,
                postId = 1,
                userId = 1,
                content = "이것은 예시 댓글입니다. 댓글 내용이 길어지면 어떻게 보이는지 확인하기 위한 텍스트입니다.",
                createdAt = "2024-03-20"
            )
        )
    }
}