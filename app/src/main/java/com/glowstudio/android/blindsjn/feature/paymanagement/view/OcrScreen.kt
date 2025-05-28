package com.glowstudio.android.blindsjn.feature.paymanagement.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.ui.theme.*

@Composable
fun OcrScreen(
    onCaptureClick: () -> Unit = {}
) {
    val verticalPadding = 48.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // 상단에 프레임
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = verticalPadding)
                .size(width = 320.dp, height = 520.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 4.dp.toPx()
                val length = 40.dp.toPx()
                val w = size.width
                val h = size.height
                val color = Blue
                drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
                drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)
                drawLine(color, Offset(w, 0f), Offset(w - length, 0f), strokeWidth)
                drawLine(color, Offset(w, 0f), Offset(w, length), strokeWidth)
                drawLine(color, Offset(0f, h), Offset(0f, h - length), strokeWidth)
                drawLine(color, Offset(0f, h), Offset(length, h), strokeWidth)
                drawLine(color, Offset(w, h), Offset(w - length, h), strokeWidth)
                drawLine(color, Offset(w, h), Offset(w, h - length), strokeWidth)
            }
            Text(
                "문서를 이 영역에 맞춰주세요.",
                color = TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // 하단에 버튼
        Button(
            onClick = onCaptureClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = verticalPadding)
                .size(72.dp)
                .clip(CircleShape)
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun OcrScreenPreview() {
    BlindSJNTheme {
        OcrScreen()
    }
}

