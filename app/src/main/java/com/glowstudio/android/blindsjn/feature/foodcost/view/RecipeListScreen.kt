package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import com.glowstudio.android.blindsjn.feature.foodcost.RegisterRecipeScreen
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.FloatingActionButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onEditRecipeClick: (String) -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {}
) {
    val viewModel: com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel = viewModel()
    val recipeList by viewModel.recipeList.collectAsState()
    var startAnimation by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val businessId = 1

    LaunchedEffect(Unit) {
        viewModel.getRecipeList(1)
        startAnimation = true
    }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            coroutineScope.launch {
                bottomSheetState.expand()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "레시피",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            
            // 헤더
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "레시피 이름",
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "판매가",
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "원가",
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "마진",
                    modifier = Modifier.weight(2f).padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.width(48.dp)) // 수정 버튼 공간
            }
            Divider(color = DividerGray, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            // 레시피 목록
            recipeList.forEach { recipe ->
                val margin = recipe.price - recipe.margin_info.total_ingredient_price.toInt()
                val marginRate = if (recipe.price > 0) (margin * 100f / recipe.price).toInt() else 0
                
                val animatedProgress by animateFloatAsState(
                    targetValue = if (startAnimation) marginRate / 100f else 0f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = FastOutSlowInEasing
                    ),
                    label = "progressAnimation"
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.title,
                        modifier = Modifier.weight(2f),
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "%,d".format(recipe.price),
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "%,d".format(recipe.margin_info.total_ingredient_price.toInt()),
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Column(
                        modifier = Modifier.weight(2f).padding(start = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.7f)
                                .height(12.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .background(Blue, RoundedCornerShape(6.dp))
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${margin}원 (${marginRate}%)",
                            fontSize = 12.sp,
                            color = Blue
                        )
                    }
                    IconButton(
                        onClick = { onEditRecipeClick(recipe.title) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // 하단 버튼
        FloatingActionButton(
            onClick = { showBottomSheet = true },
            containerColor = Blue,
            contentColor = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "레시피 추가")
        }

        // 하단 시트
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                RegisterRecipeScreen(
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.getRecipeList(1) // 레시피 목록 새로고침
                    },
                    onTextFieldFocus = {
                        coroutineScope.launch {
                            bottomSheetState.expand()
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreenPreview() {
    RecipeListScreen()
} 