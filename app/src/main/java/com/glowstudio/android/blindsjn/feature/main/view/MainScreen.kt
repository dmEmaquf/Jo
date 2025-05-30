package com.glowstudio.android.blindsjn.feature.main.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarViewModel
import com.glowstudio.android.blindsjn.feature.main.viewmodel.NavigationViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.feature.board.view.BoardScreen
import com.glowstudio.android.blindsjn.feature.board.view.BoardDetailScreen
import com.glowstudio.android.blindsjn.feature.board.view.WritePostScreen
import com.glowstudio.android.blindsjn.feature.board.view.PostDetailScreen
import com.glowstudio.android.blindsjn.feature.home.view.HomeScreen
import com.glowstudio.android.blindsjn.feature.profile.ProfileScreen
import com.glowstudio.android.blindsjn.feature.calendar.MessageScreen
import com.glowstudio.android.blindsjn.ui.screens.AddScheduleScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLDecoder
import com.google.gson.Gson
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import com.glowstudio.android.blindsjn.feature.home.view.NewsDetailScreen
import com.glowstudio.android.blindsjn.data.model.Article
import com.glowstudio.android.blindsjn.data.network.Network
import com.glowstudio.android.blindsjn.feature.paymanagement.repository.PayManagementRepository
import com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel.PayManagementViewModel
import com.glowstudio.android.blindsjn.feature.paymanagement.view.PayManagementScreen
import com.glowstudio.android.blindsjn.feature.ocr.view.OcrScreen
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.feature.foodcost.view.FoodCostScreen
import com.glowstudio.android.blindsjn.feature.foodcost.RegisterRecipeScreen
import com.glowstudio.android.blindsjn.feature.foodcost.RegisterIngredientScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.RecipeListScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.EditRecipeScreen
import com.glowstudio.android.blindsjn.feature.foodcost.view.IngredientListScreen
import com.glowstudio.android.blindsjn.feature.main.model.NavigationState
import com.glowstudio.android.blindsjn.feature.main.viewmodel.BottomBarViewModel
import com.glowstudio.android.blindsjn.feature.certification.BusinessCertificationScreen
import com.glowstudio.android.blindsjn.feature.home.NewsListScreen
import com.glowstudio.android.blindsjn.feature.ocr.view.DailySalesScreen

/**
 * 메인 스크린: 상단바, 하단 네비게이션 바, 내부 컨텐츠(AppNavHost)를 포함하여 전체 화면 전환을 관리합니다.
 */

@Composable
fun MainScreen(
    topBarViewModel: TopBarViewModel = viewModel(),
    navigationViewModel: NavigationViewModel = viewModel(),
    bottomBarViewModel: BottomBarViewModel = viewModel()
) {
    val context = LocalContext.current
    val payManagementViewModel = remember {
        val api = Network.payManagementApiService
        val repository = PayManagementRepository(api, context)
        PayManagementViewModel(repository)
    }

    // 하나의 NavController 생성
    val navController = rememberNavController()
    // TopBarViewModel에서 상단바 상태를 관찰
    val topBarState by topBarViewModel.topBarState.collectAsState()
    
    // 현재 라우트 변경 감지
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 라우트가 변경될 때마다 TopBar 상태 업데이트
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            "home", "board", "paymanagement", "foodcoast", "message", "profile" -> {
                topBarViewModel.setMainBar()
                bottomBarViewModel.showBottomBar()
            }
            else -> {
                val title = when {
                    currentRoute?.startsWith("postDetail/") == true -> "게시글"
                    currentRoute?.startsWith("boardDetail/") == true -> "게시판"
                    currentRoute?.startsWith("editRecipe/") == true -> "레시피 수정"
                    currentRoute?.startsWith("news_main/") == true -> "뉴스 메인"
                    currentRoute?.startsWith("news_detail/") == true -> "뉴스 상세"
                    else -> ""
                }
                topBarViewModel.setDetailBar(
                    title = title,
                    onBackClick = { navController.navigateUp() }
                )
                bottomBarViewModel.hideBottomBar()
            }
        }
    }

    val bottomBarRoutes = NavigationState().items.map { it.route }
    val isBottomBarVisible by bottomBarViewModel.isBottomBarVisible.collectAsState()

    Scaffold(
        // 상단바: TopBarViewModel의 상태를 기반으로 동적으로 업데이트됨
        topBar = {
            TopBar(state = topBarState)
        },
        // 하단 네비게이션 바
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigationBar(
                    navController = navController,
                    viewModel = navigationViewModel
                )
            }
        },
        // 내부 컨텐츠: NavHost에 navController와 TopBarViewModel 전달
        content = { paddingValues ->
            // paddingValues에 추가 top padding(예: 16.dp)을 더해 상단바와의 여백을 확보합니다.
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") { HomeScreen(navController) }
                    composable("board") { BoardScreen(navController) }
                    composable("paymanagement") {
                        PayManagementScreen(
                            viewModel = payManagementViewModel,
                            onNavigateToFoodCost = { navController.navigate("foodcoast") },
                            onNavigateToSalesInput = { /* TODO: Implement Sales Input navigation */ },
                            onNavigateToOcr = { navController.navigate("ocr") }
                        )
                    }
                    composable("message") { MessageScreen(navController) }
                    composable("profile") { ProfileScreen(
                        onLogoutClick = { /* ... */ },
                        onBusinessCertificationClick = { navController.navigate("businessCertification") },
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
                        route = "writePost/{category}/{tags}",
                        arguments = listOf(
                            navArgument("category") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("tags") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: ""
                        val tags = backStackEntry.arguments?.getString("tags")
                        WritePostScreen(navController, category, tags)
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
                        val articleJson = backStackEntry.arguments?.getString("articleJson") ?: ""
                        val article = Gson().fromJson(articleJson, Article::class.java)
                        NewsDetailScreen(
                            title = article.title ?: "",
                            content = article.content,
                            description = article.description,
                            imageUrl = article.urlToImage,
                            link = article.link
                        )
                    }
                    composable(
                        route = "news_list/{topic}",
                        arguments = listOf(
                            navArgument("topic") {
                                type = NavType.StringType
                                defaultValue = "자영업"
                            }
                        )
                    ) { backStackEntry ->
                        val topic = backStackEntry.arguments?.getString("topic") ?: "자영업"
                        NewsListScreen(navController = navController, selectedTopic = topic)
                    }
                    composable("businessCertification") {
                        BusinessCertificationScreen(
                            onConfirm = { /* TODO: 인증 성공 처리 */ },
                            onDismiss = { navController.navigateUp() }
                        )
                    }
                    composable("ocr") {
                        OcrScreen(
                            onCaptureClick = { navController.navigate("dailySalesScreen") }
                        )
                    }
                    composable("dailySalesScreen") {
                        DailySalesScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
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
