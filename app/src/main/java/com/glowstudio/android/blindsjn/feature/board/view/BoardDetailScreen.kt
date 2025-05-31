package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.board.model.Post
import com.glowstudio.android.blindsjn.feature.board.viewmodel.*
import androidx.compose.ui.graphics.Color
import com.glowstudio.android.blindsjn.ui.theme.*
import com.glowstudio.android.blindsjn.feature.board.view.PostBottomSheet
import com.glowstudio.android.blindsjn.feature.board.viewmodel.PostBottomSheetViewModel
import java.net.URLEncoder
import com.glowstudio.android.blindsjn.ui.theme.BackgroundWhite
import com.glowstudio.android.blindsjn.data.network.UserManager
import androidx.compose.runtime.collectAsState
import com.glowstudio.android.blindsjn.data.model.IndustryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(navController: NavController, title: String) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState(initial = "")
    val coroutineScope = rememberCoroutineScope()
    val userId by UserManager.userId.collectAsState(initial = -1)

    var selectedCategory by remember { mutableStateOf("모든 분야") }
    val categories = listOf("모든 분야", "음식점 및 카페", "쇼핑 및 리테일", "건강 및 의료", "숙박 및 여행", "교육 및 학습","여가 및 오락","금융 및 공공기관")

    val postBottomSheetViewModel: PostBottomSheetViewModel = viewModel()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = BackgroundWhite
        ) {
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
                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                val filteredPosts = posts.filter { post ->
                    if (selectedCategory == "모든 분야") {
                        true
                    } else {
                        val industryId = IndustryData.industries.entries.find { it.value == selectedCategory }?.key
                        post.industryId == industryId
                    }
                }

                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (userId != -1) {
                    PostList(navController, filteredPosts, viewModel, userId)
                }
            }
        }
    )
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                text = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
