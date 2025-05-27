package com.glowstudio.android.blindsjn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.toArgb


// 다크 모드 색상 팔레트
private val DarkColorScheme = darkColorScheme(
    primary = Blue,              // 메인 색상 (파란색)
    primaryContainer = DarkBlue,
    secondary = LightBlue,       // 보조 색상 (밝은 파란색)
    tertiary = Blue,            // 보조 색상 (파란색)
    background = DarkBackground, // 배경색 (다크 모드)
    surface = DarkSurface,       // 카드 배경 (다크 모드)
    onPrimary = White,          // 버튼 텍스트 (흰색)
    onSecondary = White,        // 보조 버튼 텍스트 (흰색)
    onTertiary = White,         // 보조 텍스트 (흰색)
    onBackground = White,        // 일반 텍스트 색상 (흰색)
    onSurface = White,          // 카드 내 텍스트 색상 (흰색)
    onSurfaceVariant = Color(0xFFBBBBBB), // 보조 텍스트 (회색)
    surfaceVariant = DarkSurface,// 카드 변형 색상 (다크)
    outline = Color(0xFF3C3C3C), // 구분선 색상
    error = Error
)

// 라이트 모드 색상 팔레트
private val LightColorScheme = lightColorScheme(
    primary = Blue,              // 메인 색상 (파란색)
    primaryContainer = LightBlue,
    secondary = DarkBlue,        // 보조 색상 (진한 파란색)
    tertiary = Blue,            // 보조 색상 (파란색)
    background = BackgroundWhite,// 배경색 (흰색)
    surface = CardWhite,         // 카드 배경 (흰색)
    onPrimary = White,          // 버튼 텍스트 (흰색)
    onSecondary = White,        // 보조 버튼 텍스트 (흰색)
    onTertiary = White,         // 보조 텍스트 (흰색)
    onBackground = TextPrimary,  // 일반 텍스트 색상 (진한 회색)
    onSurface = TextPrimary,    // 카드 내 텍스트 색상 (진한 회색)
    onSurfaceVariant = TextSecondary, // 보조 텍스트 (회색)
    surfaceVariant = CardWhite,  // 카드 변형 색상 (흰색)
    outline = DividerGray,       // 구분선 색상
    error = Error
)

@Composable
fun BlindSJNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = colorScheme.primary.toArgb()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
