package com.glowstudio.android.blindsjn.feature.ocr.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.feature.ocr.view.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    onCaptureClick: () -> Unit = {}
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cameraPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
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
            if (cameraPermissionGranted) {
                CameraPreview(modifier = Modifier.matchParentSize())
            }
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
            onClick = { showBottomSheet = true },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = verticalPadding)
                .size(72.dp)
                .clip(CircleShape)
        ) {}

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                DailySalesBottomSheet(
                    onDismiss = { showBottomSheet = false },
                    onSaved = { showBottomSheet = false }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OcrScreenPreview() {
    BlindSJNTheme {
        OcrScreen()
    }
}

