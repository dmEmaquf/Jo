package com.glowstudio.android.blindsjn.ui.components.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.ui.NewsViewModel
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import com.glowstudio.android.blindsjn.ui.theme.CardWhite
import com.glowstudio.android.blindsjn.ui.theme.DividerGray
import com.google.gson.Gson
import java.net.URLEncoder
import androidx.core.text.HtmlCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalContext

// 주제 목록 정의
private val newsTopics = listOf(
    "자영업",
    "음식점",
    "카페",
    "소상공인",
    "창업"
)

@Composable
fun NaverNewsSection(navController: NavHostController) {
    val viewModel: NewsViewModel = viewModel()
    val uiState by viewModel.uiState
    val context = LocalContext.current
    var selectedTopic by remember { 
        mutableStateOf(viewModel.loadSelectedTopic(context))
    }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTopic) {
        viewModel.searchNews(selectedTopic)
    }

    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "새로운 ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // 주제 선택 콤보박스
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { expanded = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = selectedTopic,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "▾",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                newsTopics.forEach { topic ->
                                    DropdownMenuItem(
                                        text = { Text(topic) },
                                        onClick = {
                                            selectedTopic = topic
                                            viewModel.saveSelectedTopic(context, topic)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = " 소식",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    // 더보기 버튼
                    IconButton(onClick = {
                        navController.navigate("news_list/${selectedTopic}")
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "더보기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(uiState.error ?: "오류", color = Color.Red)
                }
                else -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.newsList) { article ->
                            Surface(
                                modifier = Modifier
                                    .width(280.dp)
                                    .clickable {
                                        val articleJson = URLEncoder.encode(Gson().toJson(article), "UTF-8")
                                        navController.navigate("news_detail/$articleJson")
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = CardWhite
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = HtmlCompat.fromHtml(article.title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = HtmlCompat.fromHtml(article.description, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsListItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NaverNewsSectionPreview() {
    BlindSJNTheme {
        val navController = rememberNavController()
        NaverNewsSection(navController = navController)
    }
} 