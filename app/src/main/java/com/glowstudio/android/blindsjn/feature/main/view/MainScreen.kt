package com.glowstudio.android.blindsjn.feature.main.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.ui.navigation.mainNavGraph
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarViewModel
import com.glowstudio.android.blindsjn.feature.main.viewmodel.NavigationViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.feature.main.view.TopBarMain
import com.glowstudio.android.blindsjn.feature.main.view.TopBarDetail
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarType
import com.glowstudio.android.blindsjn.feature.board.view.BoardScreen
import com.glowstudio.android.blindsjn.feature.board.view.BoardDetailScreen
import com.glowstudio.android.blindsjn.feature.board.view.WritePostScreen
import com.glowstudio.android.blindsjn.feature.board.view.PostDetailScreen
import com.glowstudio.android.blindsjn.feature.home.HomeScreen
import com.glowstudio.android.blindsjn.feature.profile.ProfileScreen
import com.glowstudio.android.blindsjn.feature.popular.PopularScreen
import com.glowstudio.android.blindsjn.feature.calendar.MessageScreen
import com.glowstudio.android.blindsjn.ui.screens.AddScheduleScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder
import com.google.gson.Gson
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import com.glowstudio.android.blindsjn.ui.components.banner.BannerSection
import com.glowstudio.android.blindsjn.ui.components.shortcut.ShortcutSection
import com.glowstudio.android.blindsjn.ui.components.news.NaverNewsSection
import com.glowstudio.android.blindsjn.ui.components.hotpost.HotPostsSection
import com.glowstudio.android.blindsjn.ui.components.sales.SalesSection
import com.glowstudio.android.blindsjn.feature.home.NewsDetailScreen
import com.glowstudio.android.blindsjn.data.model.Article
import com.glowstudio.android.blindsjn.feature.paymanagement.PayManagementScreen
import com.glowstudio.android.blindsjn.feature.foodcost.FoodCostScreen
import com.glowstudio.android.blindsjn.feature.foodcost.RegisterRecipeScreen
import com.glowstudio.android.blindsjn.feature.foodcost.RegisterIngredientScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.RecipeListScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.EditRecipeScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.IngredientListScreen

/**
 * 메인 스크린: 상단바, 하단 네비게이션 바, 내부 컨텐츠(AppNavHost)를 포함하여 전체 화면 전환을 관리합니다.
 */

@Composable
fun MainScreen(
    topBarViewModel: TopBarViewModel = viewModel(),
    navigationViewModel: NavigationViewModel = viewModel()
) {
    // 하나의 NavController 생성
    val navController = rememberNavController()
    // TopBarViewModel에서 상단바 상태를 관찰
    val topBarState by topBarViewModel.topBarState.collectAsState()

    Scaffold(
        // 상단바: TopBarViewModel의 상태를 기반으로 동적으로 업데이트됨
        topBar = {
            TopBar(state = topBarState)
        },
        // 하단 네비게이션 바
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                viewModel = navigationViewModel
            )
        },
        // 내부 컨텐츠: NavHost에 navController와 TopBarViewModel 전달
        content = { paddingValues ->
            // paddingValues에 추가 top padding(예: 16.dp)을 더해 상단바와의 여백을 확보합니다.
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = "main_nav"
                ) {
                    composable("home") { HomeScreen(navController) }
                    composable("board") { BoardScreen(navController) }
                    composable("paymanagement") {
                        PayManagementScreen(
                            onNavigateToFoodCost = { navController.navigate("foodcoast") }
                        )
                    }
                    composable("message") { MessageScreen(navController) }
                    composable("profile") { ProfileScreen(
                        onLogoutClick = { /* ... */ },
                        onBusinessCertificationClick = { /* ... */ },
                        onProfileEditClick = { /* ... */ },
                        onContactEditClick = { /* ... */ }
                    ) }
                    composable("foodcoast") {
                        FoodCostScreen(
                            onRecipeListClick = { navController.navigate("recipeList") },
                            onRegisterRecipeClick = { navController.navigate("registerRecipe") },
                            onIngredientListClick = { navController.navigate("ingredientList") },
                            onRegisterIngredientClick = { navController.navigate("registerIngredient") },
                            onNavigateToPayManagement = { navController.navigate("paymanagement") },
                            onNavigateToFoodCost = { navController.navigate("foodcoast") }
                        )
                    }
                    composable("recipeList") {
                        RecipeListScreen(
                            onEditRecipeClick = { recipeName -> navController.navigate("editRecipe/$recipeName") },
                            onRegisterRecipeClick = { navController.navigate("registerRecipe") }
                        )
                    }
                    composable("editRecipe/{recipeName}") { backStackEntry ->
                        val recipeName = backStackEntry.arguments?.getString("recipeName") ?: ""
                        EditRecipeScreen(
                            recipeName = recipeName,
                            onEditIngredientClick = { /* TODO: 재료 수정 화면으로 이동 */ },
                            onSaveClick = { navController.popBackStack() }
                        )
                    }
                    composable("ingredientList") {
                        IngredientListScreen(
                            onEditIngredientClick = { /* TODO: 재료 수정 화면으로 이동 */ },
                            onRegisterIngredientClick = { navController.navigate("registerIngredient") }
                        )
                    }
                    composable("registerIngredient") { RegisterIngredientScreen() }
                    composable("registerRecipe") { RegisterRecipeScreen() }
                    composable("boardDetail/{title}") { backStackEntry ->
                        val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
                        val title = URLDecoder.decode(encodedTitle, "UTF-8")
                        BoardDetailScreen(navController, title)
                    }
                    composable(
                        route = "writePost?tags={tags}",
                        arguments = listOf(
                            navArgument("tags") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val tags = backStackEntry.arguments?.getString("tags")
                        WritePostScreen(navController, tags)
                    }
                    composable(
                        route = "postDetail/{postId}",
                        arguments = listOf(
                            navArgument("postId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId") ?: ""
                        PostDetailScreen(navController, postId)
                    }
                    composable("addSchedule") {
                        AddScheduleScreen(
                            onCancel = { navController.navigateUp() },
                            onSave = { schedule ->
                                // TODO: 일정 저장 로직 구현
                                navController.navigateUp()
                            }
                        )
                    }
                    composable("news_detail/{articleJson}") { backStackEntry ->
                        val articleJson = backStackEntry.arguments?.getString("articleJson")
                        val article = try {
                            Gson().fromJson(URLDecoder.decode(articleJson, "UTF-8"), Article::class.java)
                        } catch (e: Exception) {
                            null
                        }

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
                    mainNavGraph(
                        navController = navController,
                        topBarViewModel = topBarViewModel
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BlindSJNTheme {
        MainScreen()
    }
}
