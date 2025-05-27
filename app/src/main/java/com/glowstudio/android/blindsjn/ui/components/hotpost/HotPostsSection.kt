package com.glowstudio.android.blindsjn.ui.components.hotpost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout
import com.glowstudio.android.blindsjn.ui.theme.CardWhite
import com.glowstudio.android.blindsjn.ui.theme.ChatBlue
import com.glowstudio.android.blindsjn.ui.theme.DividerGray
import com.glowstudio.android.blindsjn.ui.theme.Error

data class HotPost(
    val title: String,
    val date: String,
    val likeCount: Int,
    val commentCount: Int
)

@Composable
fun HotPostsSection(navController: NavHostController) {
    // 예시 데이터
    val hotPosts = listOf(
        HotPost("오늘 점심시간에 손님이 너무 많아서 힘들었어요...", "05/13", 156, 42),
        HotPost("새로 나온 떡볶이 레시피 공유합니다!", "05/15", 89, 23),
        HotPost("주말 알바 구합니다 (시급 12,000원)", "05/11", 45, 12),
        HotPost("원가 계산하는 방법 알려주세요", "05/15", 67, 35)
    )

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionLayout(
            title = "인기글",
            onMoreClick = { navController.navigate("popular") }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    hotPosts.forEachIndexed { idx, post ->
                        HotPostListItem(post)
                        if (idx != hotPosts.lastIndex) {
                            Divider(
                                color = DividerGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotPostListItem(post: HotPost) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(post.title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ThumbUp, contentDescription = "좋아요", tint = Error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${post.likeCount}", color = Error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "댓글", tint = ChatBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${post.commentCount}", color = ChatBlue, style = MaterialTheme.typography.bodySmall)
        }
    }
} 