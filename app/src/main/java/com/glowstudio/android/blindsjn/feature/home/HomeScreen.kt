package com.glowstudio.android.blindsjn.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import com.glowstudio.android.blindsjn.ui.components.banner.BannerSection
import com.glowstudio.android.blindsjn.ui.components.shortcut.ShortcutSection
import com.glowstudio.android.blindsjn.ui.components.news.NaverNewsSection
import com.glowstudio.android.blindsjn.ui.components.hotpost.HotPostsSection
import com.glowstudio.android.blindsjn.ui.components.sales.SalesSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {

        // 배너 섹션
        BannerSection()

        // 바로가기 섹션
        // ShortcutSection(navController)

        // 네이버 뉴스 섹션
        NaverNewsSection(navController)

        // 인기글 섹션
        HotPostsSection(navController)

        // 오늘의 매출 관리 섹션
        SalesSection()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    BlindSJNTheme {
        HomeScreen(navController = navController)
    }
}