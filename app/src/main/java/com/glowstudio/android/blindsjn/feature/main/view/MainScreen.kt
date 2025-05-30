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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

/**
 * 메인 스크린: 상단바, 하단 네비게이션 바, 내부 컨텐츠(AppNavHost)를 포함하여 전체 화면 전환을 관리합니다.
 */

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    topBarViewModel: TopBarViewModel = viewModel(),
    navigationViewModel: NavigationViewModel = viewModel(),
    bottomBarViewModel: BottomBarViewModel = viewModel()
) {
    // 상태 변수는 반드시 최상단에 선언
    var showTagSearchScreen by remember { mutableStateOf(false) }
    var selectedSearchTags by remember { mutableStateOf<List<String>>(emptyList()) }
    val allTags = listOf("예비사장님", "알바/직원", "손님", "고민글", "정보", "질문/조언", "후기", "초보사장님", "고수사장님")
    var searchText by remember { mutableStateOf("") }

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
                topBarViewModel.setMainBar(
                    onSearchClick = { showTagSearchScreen = true },
                    onMoreClick = { /* 더보기 */ },
                    onNotificationClick = { /* 알림 */ }
                )
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
            Column {
                TopBar(state = topBarState)
            }
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
                    composable("board") {
                        BoardScreen(
                            navController = navController,
                            onSearchClick = { showTagSearchScreen = true },
                            selectedTags = selectedSearchTags,
                            searchText = searchText
                        )
                    }
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
                            onCaptureClick = { /* TODO: 카메라 캡처 구현 */ }
                        )
                    }
                }
            }
        }
    )

    // 태그 검색 전체화면
    if (showTagSearchScreen) {
        SearchTagScreen(
            allTags = allTags,
            initialSelectedTags = selectedSearchTags,
            onClose = { showTagSearchScreen = false },
            onApply = { text, tags ->
                selectedSearchTags = tags
                searchText = text
                showTagSearchScreen = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BlindSJNTheme {
        MainScreen()
    }
}

// 새로운 태그 검색 전체화면 Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchTagScreen(
    allTags: List<String>,
    initialSelectedTags: List<String> = emptyList(),
    onClose: () -> Unit,
    onApply: (String, List<String>) -> Unit
) {
    var selectedTags by remember { mutableStateOf(initialSelectedTags) }
    var searchText by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp) // FAB 공간 없음
            ) {
                // 1~2. 상단 서치바 + 선택된 태그를 파란색 배경 Column으로 묶음
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    // 1. 상단 서치바
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                                TextField(
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                    },
                                    placeholder = { Text("궁금한 게시글 제목을 입력하세요", color = MaterialTheme.colorScheme.primary) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.primary,
                                        unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(start = 8.dp, end = 8.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    singleLine = true,
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            onApply(searchText, selectedTags)
                                            onClose()
                                        }
                                    )
                                )
                            }
                        }
                        // X(닫기) 버튼
                        IconButton(
                            onClick = {
                                onApply("", selectedTags)
                                onClose()
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                    // 2. 선택된 태그 LazyRow (검색창 바로 아래, 고정 높이)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedTags = selectedTags - tag
                                        },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "태그 삭제",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // 3. Spacer
                Spacer(Modifier.height(16.dp))
                // 4. 전체 태그 FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    allTags.forEach { tag ->
                        val selected = selectedTags.contains(tag)
                        Surface(
                            onClick = {
                                if (selected) {
                                    selectedTags = selectedTags - tag
                                } else if (!selectedTags.contains(tag)) {
                                    selectedTags = selectedTags + tag
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                        ) {
                            Text(
                                text = tag,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.White, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
