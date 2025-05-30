package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.glowstudio.android.blindsjn.feature.board.viewmodel.PostViewModel
import com.glowstudio.android.blindsjn.feature.board.viewmodel.BoardViewModel
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import com.glowstudio.android.blindsjn.feature.board.viewmodel.WritePostViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.ui.theme.BackgroundWhite
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import android.util.Log
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen(
    navController: NavController,
    industry: String = "",
    tags: String? = null
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel()
    val boardViewModel: BoardViewModel = viewModel()
    val writePostViewModel = remember { WritePostViewModel(boardViewModel) }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var selectedNonEnabledCategory by remember { mutableStateOf<BoardCategory?>(null) }
    val categories by writePostViewModel.categories.collectAsState()
    val selectedCategory by writePostViewModel.selectedCategory.collectAsState()
    val selectedTags by writePostViewModel.selectedTags.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var isQuestion by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<Int?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var showCertificationDialog by remember { mutableStateOf(false) }
    val statusMessage: String = viewModel.statusMessage.collectAsState(initial = "").value
    val snackbarHostState = remember { SnackbarHostState() }

    val contentFocusRequester = remember { FocusRequester() }

    val shouldNavigateBack by viewModel.shouldNavigateBack.collectAsState()

    LaunchedEffect(Unit) {
        userId = UserManager.getUserId(context).first()
        phoneNumber = UserManager.getPhoneNumber(context)
        boardViewModel.checkCertification(context)
    }

    LaunchedEffect(tags) {
        tags?.let { tagString ->
            val tagList = tagString.split(",")
            writePostViewModel.setSelectedTags(tagList)
        }
    }

    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            viewModel.resetNavigation()
            navController.popBackStack()
        }
    }

    // 게시글 저장 함수
    fun savePost() {
        if (title.isBlank() || content.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "제목과 내용을 입력해주세요",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }

        if (userId != null && phoneNumber != null) {
            viewModel.savePost(
                title = title,
                content = content,
                userId = userId!!,
                industry = selectedCategory?.title ?: industry,
                industryId = selectedCategory?.id,
                phoneNumber = phoneNumber!!,
                experience = "신입",
                tags = selectedTags.toList()  // 선택된 태그 전달
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        content = {
                            Text(
                                text = data.visuals.message,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clickable { expanded = true }
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedCategory?.emoji ?: "📝",
                                style = MaterialTheme.typography.titleLarge
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { category ->
                                    val isEnabled = boardViewModel.isCategoryEnabled(category)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = category.title,
                                                color = if (isEnabled)
                                                    MaterialTheme.colorScheme.onSurface
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        },
                                        onClick = {
                                            if (isEnabled) {
                                                writePostViewModel.selectCategory(category)
                                                expanded = false
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "사업자 인증이 필요한 업종입니다",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                expanded = false
                                            }
                                        },
                                        leadingIcon = {
                                            Text(
                                                text = category.emoji,
                                                color = if (isEnabled)
                                                    MaterialTheme.colorScheme.onSurface
                                                else
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        },
                                        enabled = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("제목을 입력해주세요.") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { contentFocusRequester.requestFocus() })
                        )
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("자유롭게 얘기해보세요.\n#질문 #고민", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .focusRequester(contentFocusRequester),
                        maxLines = Int.MAX_VALUE,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(onClick = { /* 이미지 첨부 */ }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "이미지 첨부")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { /* 파일 첨부 */ }) {
                                Icon(Icons.Default.AttachFile, contentDescription = "파일 첨부")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        CommonButton(
                            text = "작성",
                            onClick = {
                                if (selectedCategory == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "카테고리를 선택해주세요",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else if (title.isBlank() || content.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "제목과 내용을 입력하세요",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else if (phoneNumber == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "전화번호 정보를 찾을 수 없습니다",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    userId?.let { id ->
                                        val currentCategory = selectedCategory
                                        val categoryTitle = if (industry.isNotEmpty()) industry else currentCategory?.title ?: "자유게시판"
                                        viewModel.savePost(
                                            title = title,
                                            content = content,
                                            userId = id,
                                            industry = categoryTitle,
                                            industryId = currentCategory?.id,
                                            phoneNumber = phoneNumber!!,
                                            experience = "신입",
                                            tags = selectedTags.toList()
                                        )
                                    } ?: run {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "사용자 정보를 찾을 수 없습니다",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = selectedCategory != null
                        )
                        if (selectedCategory == null) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "카테고리를 선택해주세요",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                            )
                        }
                    }

                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun WritePostScreenPreview() {
    WritePostScreen(navController = rememberNavController())
}
