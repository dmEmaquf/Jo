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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 카메라 프레임 (네 모서리만)
        Box(
            modifier = Modifier
                .size(260.dp)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 4.dp.toPx()
                val length = 32.dp.toPx()
                val w = size.width
                val h = size.height
                val color = Blue
                // 왼쪽 위
                drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
                drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)
                // 오른쪽 위
                drawLine(color, Offset(w, 0f), Offset(w - length, 0f), strokeWidth)
                drawLine(color, Offset(w, 0f), Offset(w, length), strokeWidth)
                // 왼쪽 아래
                drawLine(color, Offset(0f, h), Offset(0f, h - length), strokeWidth)
                drawLine(color, Offset(0f, h), Offset(length, h), strokeWidth)
                // 오른쪽 아래
                drawLine(color, Offset(w, h), Offset(w - length, h), strokeWidth)
                drawLine(color, Offset(w, h), Offset(w, h - length), strokeWidth)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // 안내 문구
        Text(
            "문서를 상단의 영역에 맞춘 뒤\n촬영 버튼을 눌러주세요.",
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 40.dp),
            textAlign = TextAlign.Center
        )
        // 촬영 버튼
        Button(
            onClick = onCaptureClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            modifier = Modifier
                .size(64.dp)
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

