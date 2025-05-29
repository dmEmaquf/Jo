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
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

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
    val statusMessage: String = viewModel.statusMessage.collectAsState(initial = "").value

    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        userId = UserManager.getUserId(context).first()
        phoneNumber = UserManager.getPhoneNumber(context)
    }

    LaunchedEffect(statusMessage) {
        if (statusMessage.contains("성공") || statusMessage.contains("저장")) {
            navController.navigateUp()
        }
    }

    LaunchedEffect(tags) {
        tags?.let { tagString ->
            val tagList = tagString.split(",")
            writePostViewModel.setSelectedTags(tagList)
        }
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                            text = selectedCategory.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.title) },
                                    onClick = {
                                        writePostViewModel.selectCategory(category)
                                        expanded = false
                                    },
                                    leadingIcon = { Text(category.emoji) }
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

                    Row {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isQuestion = !isQuestion }
                        ) {
                            Checkbox(checked = isQuestion, onCheckedChange = null)
                            Text("질문")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isAnonymous = !isAnonymous }
                        ) {
                            Checkbox(checked = isAnonymous, onCheckedChange = null)
                            Text("익명")
                        }
                    }
                }

                CommonButton(
                    text = "작성",
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            viewModel.setStatusMessage("제목과 내용을 입력하세요.")
                        } else if (phoneNumber == null) {
                            viewModel.setStatusMessage("전화번호 정보를 찾을 수 없습니다.")
                        } else {
                            userId?.let { id ->
                                val categoryTitle = if (industry.isNotEmpty()) industry else selectedCategory.title
                                viewModel.savePost(
                                    title = title,
                                    content = content,
                                    userId = id,
                                    industry = categoryTitle,
                                    phoneNumber = phoneNumber!!
                                )
                            } ?: run {
                                viewModel.setStatusMessage("사용자 정보를 찾을 수 없습니다.")
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                )

                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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
