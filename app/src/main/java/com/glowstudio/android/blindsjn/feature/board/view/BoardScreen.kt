package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.feature.board.viewmodel.BoardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import com.glowstudio.android.blindsjn.ui.theme.*
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.graphics.Color
import com.glowstudio.android.blindsjn.feature.board.viewmodel.PostViewModel
import com.glowstudio.android.blindsjn.feature.board.model.Post
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import com.glowstudio.android.blindsjn.feature.board.view.PostBottomSheet
import com.glowstudio.android.blindsjn.feature.board.viewmodel.PostBottomSheetViewModel
import com.glowstudio.android.blindsjn.utils.TimeUtils
import androidx.compose.ui.text.style.TextOverflow
import com.glowstudio.android.blindsjn.feature.board.view.CategoryBottomSheet


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BoardScreen(navController: NavController) {
    val boardViewModel: BoardViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val boardCategories by boardViewModel.boardCategories.collectAsState()
    val posts by postViewModel.posts.collectAsState()
    val statusMessage by postViewModel.statusMessage.collectAsState()
    var showCategorySheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BoardCategory?>(null) }
    // 글쓰기 바텀시트 상태
    var showSheet by remember { mutableStateOf(false) }
    val postBottomSheetViewModel: PostBottomSheetViewModel = viewModel()

    // 실제 로그인 유저 id로 교체 필요
    val userId = 1234 // 예시: 실제 로그인 유저 id로 대체

    // 게시글 항상 불러오기
    LaunchedEffect(Unit) {
        postViewModel.loadPosts()
    }

    // 업종 7개만 추출
    val industryCategories = boardCategories.filter { it.group == "업종" }

    // 카테고리 필터링 + 최신순 정렬
    val filteredPosts = (selectedCategory?.let { cat ->
        posts.filter { it.category == cat.title }
    } ?: posts).sortedByDescending { it.time }

    // 카테고리 바텀시트
    if (showCategorySheet) {
        CategoryBottomSheet(
            categories = boardCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    // 글쓰기 바텀시트
    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = BackgroundWhite) {
            val tags by postBottomSheetViewModel.tags.collectAsState()
            val enabledTags by postBottomSheetViewModel.enabledTags.collectAsState()
            val selectedTags by postBottomSheetViewModel.selectedTags.collectAsState()
            PostBottomSheet(
                tags = tags,
                enabledTags = enabledTags,
                onDone = {
                    showSheet = false
                    val encodedTags = URLEncoder.encode(it.joinToString(","), "UTF-8")
                    navController.navigate("write_post_screen/$encodedTags")
                    postBottomSheetViewModel.clearSelection()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp), // 화살표 공간 확보
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 전체 필터칩 (고정)
                    CustomFilterChip(
                        text = "전체",
                        isSelected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                    
                    // 스크롤 가능한 업종 카테고리
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(industryCategories) { category ->
                            CustomFilterChip(
                                text = category.title,
                                isSelected = selectedCategory?.title == category.title,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(CardWhite, MaterialTheme.shapes.medium)
                        .height(32.dp)
                ) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "카테고리 전체 보기")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "글쓰기")
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (!statusMessage.isNullOrEmpty()) {
                    Text(
                        text = statusMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                PostList(navController, filteredPosts, postViewModel, userId)
            }
        }
    )
}

@Composable
fun BoardCategoryItem(category: BoardCategory, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp)
            .border(1.dp, DividerGray, MaterialTheme.shapes.medium)
            .background(CardWhite, MaterialTheme.shapes.medium)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Blue.copy(alpha = 0.1f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = category.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun CustomFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Blue else DividerGray,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) CardWhite else TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BoardScreenPreview() {
    BlindSJNTheme {
        val navController = rememberNavController()
        BoardScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun BoardCategoryItemPreview() {
    BlindSJNTheme {
        BoardCategoryItem(
            category = BoardCategory(
                title = "자유게시판",
                emoji = "💬",
                route = "freeBoard",
                group = "소통"
            ),
            onClick = {}
        )
    }
}

@Composable
fun PostItem(
    navController: NavController,
    post: Post,
    viewModel: PostViewModel,
    userId: Int
) {
    var isLiked by remember { mutableStateOf(post.isLiked ?: false) }
    var likeCount by remember { mutableIntStateOf(post.likeCount) }
    var isLiking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .clickable { navController.navigate("post_detail/${post.id}") }
            .padding(16.dp)
    ) {
        // 업종(카테고리)
        Text(
            text = post.category,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        // 제목
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 내용
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 시간
            Text(
                text = TimeUtils.getTimeAgo(post.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(16.dp))
            // 좋아요
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(enabled = !isLiking) {
                        isLiking = true
                        viewModel.toggleLike(post.id, userId) { success, newIsLiked, newLikeCount ->
                            if (success) {
                                isLiked = newIsLiked
                                likeCount = newLikeCount
                            }
                            isLiking = false
                        }
                    }
                    .alignByBaseline()
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "좋아요",
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // 댓글
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alignByBaseline()
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "댓글",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.commentCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PostList(
    navController: NavController,
    posts: List<Post>,
    viewModel: PostViewModel,
    userId: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(posts) { post ->
            PostItem(
                navController = navController,
                post = post,
                viewModel = viewModel,
                userId = userId
            )
        }
    }
}
