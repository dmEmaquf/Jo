package com.glowstudio.android.blindsjn.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.glowstudio.android.blindsjn.feature.board.view.BoardDetailScreen
import com.glowstudio.android.blindsjn.feature.board.view.BoardScreen
import com.glowstudio.android.blindsjn.feature.board.view.WritePostScreen
import com.glowstudio.android.blindsjn.ui.screens.*
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarViewModel
import com.glowstudio.android.blindsjn.data.model.Article
import com.glowstudio.android.blindsjn.feature.board.view.PostDetailScreen
import com.glowstudio.android.blindsjn.feature.calendar.MessageScreen
import com.glowstudio.android.blindsjn.feature.certification.BusinessCertificationScreen
import com.glowstudio.android.blindsjn.feature.home.HomeScreen
import com.glowstudio.android.blindsjn.feature.home.NewsDetailScreen
import com.glowstudio.android.blindsjn.feature.home.NewsListScreen
import com.glowstudio.android.blindsjn.feature.popular.PopularScreen
import com.glowstudio.android.blindsjn.feature.profile.ProfileScreen
import com.google.gson.Gson
import java.net.URLDecoder

fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel
) {
    navigation(
        startDestination = "home_screen",
        route = "main_nav"
    ) {
        // 홈 화면
        composable("home_screen") {
            topBarViewModel.setMainBar(
                onSearchClick = { /* 검색 */ },
                onMoreClick = { /* 더보기 */ },
                onNotificationClick = { /* 알림 */ }
            )
            HomeScreen(navController = navController)
        }

        composable("news_detail/{articleJson}") { backStackEntry ->
            val articleJson = backStackEntry.arguments?.getString("articleJson")
            val article = try {
                Gson().fromJson(URLDecoder.decode(articleJson, "UTF-8"), Article::class.java)
            } catch (e: Exception) {
                null
            }

            topBarViewModel.setDetailBar(
                title = "",
                onBackClick = { navController.navigateUp() },
                onSearchClick = { /* 검색 기능 */ },
                onMoreClick = { /* 더보기 메뉴 */ }
            )

            if (article != null) {
                NewsDetailScreen(
                    title = article.title ?: "제목 없음",
                    content = article.content,
                    description = article.description,
                    imageUrl = article.urlToImage,
                    link = article.link
                )
            }
        }

        composable("news_list/{topic}") { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: "자영업"

            topBarViewModel.setDetailBar(
                title = "뉴스 목록",
                onBackClick = { navController.navigateUp() },
                onSearchClick = { /* 검색 기능 */ },
                onMoreClick = { /* 더보기 메뉴 */ }
            )

            NewsListScreen(
                navController = navController,
                selectedTopic = topic
            )
        }

        // 게시판 네비게이션 그래프
        boardNavGraph(navController, topBarViewModel)
        
        // 인기글 네비게이션 그래프
        popularNavGraph(navController, topBarViewModel)
        
        // 메시지(캘린더) 네비게이션 그래프
        messageNavGraph(navController, topBarViewModel)
        
        // 프로필 네비게이션 그래프
        profileNavGraph(navController, topBarViewModel)
    }
}

fun NavGraphBuilder.boardNavGraph(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel
) {
    navigation(
        startDestination = "board_list_screen",
        route = "board_root"
    ) {
        composable("board_list_screen") {
            topBarViewModel.setMainBar(
                onSearchClick = { /* 검색 */ },
                onMoreClick = { /* 더보기 */ },
                onNotificationClick = { /* 알림 */ }
            )
            BoardScreen(navController = navController)
        }

        composable("board_detail/{title}") { backStackEntry ->
            val postTitle = backStackEntry.arguments?.getString("title") ?: "게시글"
            topBarViewModel.setDetailBar(
                title = postTitle,
                onBackClick = { navController.navigateUp() },
                onSearchClick = { /* 검색 기능 */ },
                onMoreClick = { /* 더보기 메뉴 */ }
            )
            BoardDetailScreen(navController = navController, title = postTitle)
        }

        composable(
            route = "write_post_screen/{tags}",
            arguments = listOf(
                navArgument("tags") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val tags = backStackEntry.arguments?.getString("tags")
            topBarViewModel.setDetailBar(
                title = "게시글 작성",
                onBackClick = { navController.navigateUp() }
            )
            WritePostScreen(navController = navController, tags = tags)
        }

        composable(
            route = "post_detail/{postId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    defaultValue = "1"
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: "1"
            topBarViewModel.setDetailBar(
                title = "게시글 상세",
                onBackClick = { navController.navigateUp() }
            )
            PostDetailScreen(navController = navController, postId = postId)
        }
    }
}

fun NavGraphBuilder.popularNavGraph(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel
) {
    navigation(
        startDestination = "popular_list_screen",
        route = "popular_root"
    ) {
        composable("popular_list_screen") {
            topBarViewModel.setMainBar(
                onSearchClick = { /* 검색 */ },
                onMoreClick = { /* 더보기 */ },
                onNotificationClick = { /* 알림 */ }
            )
            PopularScreen()
        }
    }
}

fun NavGraphBuilder.messageNavGraph(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel
) {
    navigation(
        startDestination = "calendar_screen",
        route = "message_root"
    ) {
        composable("calendar_screen") {
            topBarViewModel.setMainBar(
                onSearchClick = { /* 검색 */ },
                onMoreClick = { /* 더보기 */ },
                onNotificationClick = { /* 알림 */ }
            )
            MessageScreen(navController = navController)
        }

        composable("add_schedule_screen") {
            topBarViewModel.setDetailBar("일정 추가")
            AddScheduleScreen(
                onCancel = { navController.navigateUp() },
                onSave = { schedule ->
                    navController.navigateUp()
                }
            )
        }
    }
}

fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel
) {
    navigation(
        startDestination = "profile_main_screen",
        route = "profile_root"
    ) {
        composable("profile_main_screen") {
            topBarViewModel.setMainBar(
                onSearchClick = { /* 검색 */ },
                onMoreClick = { /* 더보기 */ },
                onNotificationClick = { /* 알림 */ }
            )
            ProfileScreen(
                onLogoutClick = {
                    navController.navigate("auth") {
                        popUpTo("main_root") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBusinessCertificationClick = { navController.navigate("certification_screen") },
                onProfileEditClick = { navController.navigate("edit_profile_screen") },
                onContactEditClick = { navController.navigate("edit_contact_screen") }
            )
        }

        composable("certification_screen") {
            topBarViewModel.setDetailBar("사업자 인증")
            BusinessCertificationScreen(
                navController = navController,
                onConfirm = { phone, certNumber, industry ->
                    navController.navigate("someNextRoute")
                }
            )
        }

        composable("edit_profile_screen") {
            topBarViewModel.setDetailBar("프로필 변경")
            EditProfileScreen(
                onBackClick = { navController.navigateUp() },
                onSave = {
                    navController.navigateUp()
                }
            )
        }

        composable("edit_contact_screen") {
            topBarViewModel.setDetailBar("연락처 변경")
            EditContactScreen(
                onBackClick = { navController.navigateUp() },
                onSave = {
                    navController.navigateUp()
                }
            )
        }
    }
} 