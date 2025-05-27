package com.glowstudio.android.blindsjn.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

object ColorPalette {
    // 메인 색상
    val Blue = Color(0xFF2196F3)         // 메인 파란색
    val LightBlue = Color(0xFF64B5F6)    // 밝은 파란색
    val DarkBlue = Color(0xFF1976D2)     // 진한 파란색
    val ChatBlue = Color(0xFF00B8D9)     // 채팅/댓글 파란색

    // 배경 및 카드 색상
    val White = Color(0xFFFFFFFF)         // 순수 흰색
    val BackgroundWhite = Color(0xFFFAFAFA) // 배경 흰색
    val CardWhite = Color(0xFFFFFFFF)      // 카드 흰색
    val DividerGray = Color(0xFFEBEBEB)    // 구분선 색상

    // 텍스트 색상
    val TextPrimary = Color(0xFF212121)    // 기본 텍스트 (거의 검정)
    val TextSecondary = Color(0xFF757575)   // 보조 텍스트 (회색)
    val TextHint = Color(0xFFBDBDBD)        // 힌트 텍스트 (연한 회색)

    // 다크 모드 색상
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)

    // 상태 표시 색상
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFE53935)
    val Warning = Color(0xFFFFA726)

    // 색상 목록
    val colors = listOf(
        "Blue" to Blue,
        "LightBlue" to LightBlue,
        "DarkBlue" to DarkBlue,
        "ChatBlue" to ChatBlue,
        "White" to White,
        "BackgroundWhite" to BackgroundWhite,
        "CardWhite" to CardWhite,
        "DividerGray" to DividerGray,
        "TextPrimary" to TextPrimary,
        "TextSecondary" to TextSecondary,
        "TextHint" to TextHint,
        "DarkBackground" to DarkBackground,
        "DarkSurface" to DarkSurface,
        "Success" to Success,
        "Error" to Error,
        "Warning" to Warning
    )
}

// 기존 Color 변수들을 ColorPalette의 프로퍼티로 참조
val Blue = ColorPalette.Blue
val LightBlue = ColorPalette.LightBlue
val DarkBlue = ColorPalette.DarkBlue
val ChatBlue = ColorPalette.ChatBlue
val White = ColorPalette.White
val BackgroundWhite = ColorPalette.BackgroundWhite
val CardWhite = ColorPalette.CardWhite
val DividerGray = ColorPalette.DividerGray
val TextPrimary = ColorPalette.TextPrimary
val TextSecondary = ColorPalette.TextSecondary
val TextHint = ColorPalette.TextHint
val DarkBackground = ColorPalette.DarkBackground
val DarkSurface = ColorPalette.DarkSurface
val Success = ColorPalette.Success
val Error = ColorPalette.Error
val Warning = ColorPalette.Warning

@Composable
fun ColorItem(name: String, color: Color) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPalettePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "컬러 팔레트",
            color = TextPrimary,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ColorPalette.colors) { (name, color) ->
                ColorItem(name, color)
            }
        }
    }
}
