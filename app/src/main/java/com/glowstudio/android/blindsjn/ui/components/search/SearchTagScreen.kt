package com.glowstudio.android.blindsjn.ui.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchTagScreen(
    allTags: List<String>,
    initialSelectedTags: List<String> = emptyList(),
    onClose: () -> Unit,
    onApply: (String, List<String>) -> Unit
) {
    var selectedTags by remember { mutableStateOf(initialSelectedTags) }
    var searchText by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp) // FAB 공간 없음
            ) {
                // 1~2. 상단 서치바 + 선택된 태그를 파란색 배경 Column으로 묶음
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    // 1. 상단 서치바
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                                TextField(
                                    value = searchText,
                                    onValueChange = { 
                                        searchText = it
                                    },
                                    placeholder = { Text("궁금한 게시글 제목을 입력하세요", color = MaterialTheme.colorScheme.primary) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(start = 8.dp, end = 8.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    singleLine = true,
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { 
                                            onApply(searchText, selectedTags)
                                            onClose()
                                        }
                                    )
                                )
                            }
                        }
                        // X(닫기) 버튼
                        IconButton(
                            onClick = {
                                onApply("", selectedTags)
                                onClose()
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                    // 2. 선택된 태그 LazyRow (검색창 바로 아래, 고정 높이)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = { 
                                            selectedTags = selectedTags - tag
                                            // onApply는 호출하지 않음
                                        },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "태그 삭제",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // 3. Spacer
                Spacer(Modifier.height(16.dp))
                // 4. 전체 태그 FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    allTags.forEach { tag ->
                        val selected = selectedTags.contains(tag)
                        Surface(
                            onClick = {
                                if (selected) {
                                    selectedTags = selectedTags - tag
                                } else if (!selectedTags.contains(tag)) {
                                    selectedTags = selectedTags + tag
                                }
                                // onApply는 호출하지 않음
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                        ) {
                            Text(
                                text = tag,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.White, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 