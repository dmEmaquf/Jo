package com.glowstudio.android.blindsjn.ui.components.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.ui.theme.*

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
        color = BackgroundWhite
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp)
            ) {
                // 상단 검색 영역
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue)
                ) {
                    // 검색바
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, Blue),
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
                                    tint = Blue,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(24.dp)
                                )
                                TextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    placeholder = { 
                                        Text(
                                            "궁금한 게시글 제목을 입력하세요",
                                            color = TextSecondary,
                                            fontSize = 16.sp
                                        ) 
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = Blue
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
                        
                        IconButton(
                            onClick = {
                                onApply("", selectedTags)
                                onClose()
                            },
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "닫기",
                                tint = CardWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // 선택된 태그 영역
                    AnimatedVisibility(
                        visible = selectedTags.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedTags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = CardWhite,
                                    border = BorderStroke(1.dp, Blue),
                                    modifier = Modifier.animateContentSize()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = Blue,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = { selectedTags = selectedTags - tag },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "태그 삭제",
                                                tint = Blue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // 전체 태그 영역
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    allTags.forEach { tag ->
                        val selected = selectedTags.contains(tag)
                        val scale = remember { Animatable(1f) }
                        
                        LaunchedEffect(selected) {
                            scale.animateTo(
                                targetValue = if (selected) 1.05f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        
                        Surface(
                            onClick = {
                                if (selected) {
                                    selectedTags = selectedTags - tag
                                } else if (!selectedTags.contains(tag)) {
                                    selectedTags = selectedTags + tag
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            color = if (selected) Blue else CardWhite,
                            border = if (!selected) BorderStroke(1.dp, Blue) else null,
                            modifier = Modifier
                                .scale(scale.value)
                                .animateContentSize()
                        ) {
                            Text(
                                text = tag,
                                color = if (selected) CardWhite else Blue,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SearchTagScreenPreview_Empty() {
    MaterialTheme {
        SearchTagScreen(
            allTags = listOf(
                "안드로이드", "iOS", "웹", "백엔드", "프론트엔드",
                "UI/UX", "디자인", "기획", "마케팅", "운영"
            ),
            initialSelectedTags = emptyList(),
            onClose = {},
            onApply = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SearchTagScreenPreview_WithSelectedTags() {
    MaterialTheme {
        SearchTagScreen(
            allTags = listOf(
                "안드로이드", "iOS", "웹", "백엔드", "프론트엔드",
                "UI/UX", "디자인", "기획", "마케팅", "운영"
            ),
            initialSelectedTags = listOf("안드로이드", "UI/UX"),
            onClose = {},
            onApply = { _, _ -> }
        )
    }
}