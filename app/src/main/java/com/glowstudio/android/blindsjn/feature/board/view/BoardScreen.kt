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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.Search
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
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BoardScreen(
    navController: NavController,
    onSearchClick: () -> Unit,
    selectedTags: List<String> = emptyList(),
    searchText: String = ""
) {
    val context = LocalContext.current
    val boardViewModel: BoardViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val boardCategories by boardViewModel.boardCategories.collectAsState()
    val posts by postViewModel.posts.collectAsState()
    val statusMessage by postViewModel.statusMessage.collectAsState(initial = "")
    var showCategorySheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BoardCategory?>(null) }
    // 글쓰기 바텀시트 상태
    var showSheet by remember { mutableStateOf(false) }
    val postBottomSheetViewModel: PostBottomSheetViewModel = viewModel()
    var userId by remember { mutableStateOf<Int?>(null) }
    var showTagSearchSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userId = UserManager.getUserId(context).first()
    }

    // 게시글 항상 불러오기
    LaunchedEffect(Unit) {
        postViewModel.loadPosts()
    }

    // 업종 7개만 추출
    val industryCategories = boardCategories.filter { it.group == "업종" }

    // 카테고리 필터링 + 태그 필터링 + 제목 검색 + 최신순 정렬
    val filteredPosts = remember(selectedCategory, posts, selectedTags, searchText) {
        val categoryFiltered = selectedCategory?.let { cat ->
            posts.filter { it.category == cat.title }
        } ?: posts
        val tagFiltered = if (selectedTags.isNotEmpty()) {
            categoryFiltered.filter { post ->
                post.tags.any { tag -> selectedTags.contains(tag) }
            }
        } else {
            categoryFiltered
        }
        val titleFiltered = if (searchText.isNotBlank()) {
            tagFiltered.filter { post ->
                post.title.contains(searchText, ignoreCase = true)
            }
        } else {
            tagFiltered
        }
        titleFiltered.sortedByDescending { it.time }
    }

    // 카테고리 바텀시트
    if (showCategorySheet) {
        CategoryBottomSheet(
            categories = boardCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                showCategorySheet = false
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
                    val encodedCategory = URLEncoder.encode(selectedCategory?.title ?: "자유게시판", "UTF-8")
                    val encodedTags = URLEncoder.encode(it.joinToString(","), "UTF-8")
                    navController.navigate("writePost/$encodedCategory/$encodedTags") {
                        launchSingleTop = true
                    }
                    postBottomSheetViewModel.clearSelection()
                }
            )
        }
    }

    // 태그 검색 바텀시트 (임시 UI)
    if (showTagSearchSheet) {
        ModalBottomSheet(onDismissRequest = { showTagSearchSheet = false }) {
            Text("여기에 태그 검색 UI가 들어갑니다.", modifier = Modifier.padding(24.dp))
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
                        onClick = { 
                            selectedCategory = null
                            showCategorySheet = false
                        }
                    )
                    
                    // 스크롤 가능한 업종 카테고리
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(industryCategories) { category ->
                            CustomFilterChip(
                                text = category.title,
                                isSelected = selectedCategory?.title == category.title,
                                onClick = { 
                                    selectedCategory = category
                                    showCategorySheet = false
                                }
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
                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                if (userId != null) {
                    PostList(navController, filteredPosts, postViewModel, userId!!)
                }
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
        BoardScreen(navController = navController, onSearchClick = { })
    }
}

@Preview(showBackground = true)
@Composable
fun BoardCategoryItemPreview() {
    BlindSJNTheme {
        BoardCategoryItem(
            category = BoardCategory.FreeBoard,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostListPreview() {
    BlindSJNTheme {
        val navController = rememberNavController()
        val viewModel: PostViewModel = viewModel()
        val previewPosts = listOf(
            Post(
                id = 1,
                title = "샘플 게시글 1",
                content = "이것은 샘플 게시글의 내용입니다.",
                category = BoardCategory.FreeBoard.title,
                time = "2024-03-20 14:30:00",
                commentCount = 5,
                likeCount = 10,
                isLiked = false,
                userId = 1
            ),
            Post(
                id = 2,
                title = "샘플 게시글 2",
                content = "두 번째 샘플 게시글의 내용입니다.",
                category = BoardCategory.PopularBoard.title,
                time = "2024-03-20 15:00:00",
                commentCount = 3,
                likeCount = 7,
                isLiked = true,
                userId = 2
            )
        )
        PostList(navController = navController, posts = previewPosts, viewModel = viewModel, userId = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun PostItemPreview() {
    BlindSJNTheme {
        val navController = rememberNavController()
        val viewModel: PostViewModel = viewModel()
        val previewPost = Post(
            id = 1,
            title = "샘플 게시글",
            content = "이것은 샘플 게시글의 내용입니다. 미리보기에서 확인할 수 있습니다.",
            category = BoardCategory.FreeBoard.title,
            time = "2024-03-20 14:30:00",
            commentCount = 5,
            likeCount = 10,
            isLiked = false,
            userId = 1
        )
        PostItem(navController = navController, post = previewPost, viewModel = viewModel, userId = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
            .clickable { navController.navigate("postDetail/${post.id}") }
            .padding(16.dp)
    ) {
        // 업종(카테고리)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = post.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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

        // 태그 표시 (내용과 메타데이터 사이)
        if (post.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 처음 2개의 태그만 표시
                post.tags.take(2).forEach { tag ->
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
                // 나머지 태그가 있다면 +n 형식으로 표시
                if (post.tags.size > 2) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "+${post.tags.size - 2}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

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
                modifier = Modifier.alignByBaseline()
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "좋아요",
                    tint = if (isLiked) Error else MaterialTheme.colorScheme.onSurface,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostItem(
    post: Post,
    onPostClick: (Int) -> Unit,
    onLikeClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .clickable { onPostClick(post.id) }
            .padding(16.dp)
    ) {
        // 업종(카테고리)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = post.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        
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
        
        // 태그 표시 (내용과 메타데이터 사이)
        if (post.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 처음 2개의 태그만 표시
                post.tags.take(2).forEach { tag ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                // 나머지 태그가 있다면 +n 형식으로 표시
                if (post.tags.size > 2) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "+${post.tags.size - 2}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 메타데이터 (시간, 좋아요, 댓글)
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
                modifier = Modifier.alignByBaseline()
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "좋아요",
                    tint = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
        }
    }
}
