package com.glowstudio.android.blindsjn.feature.main.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationState(
    val currentRoute: String = "home_screen",
    val items: List<Screen> = listOf(
        Screen.Home,
        Screen.Posts,
        Screen.Popular,
        Screen.Messages,
        Screen.Profile
    )
)

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "홈화면", Icons.Default.Home)
    object Posts : Screen("board", "게시판 목록", Icons.Default.GridView)
    object Popular : Screen("paymanagement", "매출 관리", Icons.Default.MonetizationOn)
    object Messages : Screen("message", "캘린더", Icons.Default.CalendarToday)
    object Profile : Screen("profile", "프로필", Icons.Default.Person)
} 