/*
* 뉴스 세부 기사 확인 스크린
*
*
* */


package com.glowstudio.android.blindsjn.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.core.text.HtmlCompat
import androidx.compose.ui.platform.LocalUriHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Composable
fun NewsDetailScreen(title: String, content: String?, description: String?, imageUrl: String?, link: String?) {
    val uriHandler = LocalUriHandler.current
    
    // description에서 이미지 URL 추출
    val extractedImageUrl = description?.let { desc ->
        try {
            val doc: Document = Jsoup.parse(desc)
            doc.select("img").firstOrNull()?.attr("src")
        } catch (e: Exception) {
            null
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 기존 이미지나 description에서 추출한 이미지 표시
        if (!imageUrl.isNullOrBlank() || !extractedImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl ?: extractedImageUrl,
                contentDescription = "기사 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        val bodyText = when {
            !content.isNullOrBlank() -> content
            !description.isNullOrBlank() -> description
            else -> "내용이 없습니다."
        }

        Text(
            text = HtmlCompat.fromHtml(bodyText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 원문 링크 버튼 추가
        if (!link.isNullOrBlank()) {
            Button(
                onClick = { uriHandler.openUri(link) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("네이버 뉴스에서 더 보기")
            }
        }
    }
}