package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.IngredientViewModel

@Composable
fun FoodCostDashboardScreen(
    onEditRecipeClick: (String) -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {},
    onEditIngredientClick: (String) -> Unit = {},
    onRegisterIngredientClick: () -> Unit = {}
) {
    val recipeViewModel: RecipeViewModel = viewModel()
    val ingredientViewModel: IngredientViewModel = viewModel()
    val recipeList by recipeViewModel.recipeList.collectAsState()
    val ingredients by ingredientViewModel.ingredients.collectAsState()
    
    LaunchedEffect(Unit) {
        recipeViewModel.getRecipeList(1)
        ingredientViewModel.loadIngredients()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        // 상단 마진 요약 카드
        item {
            MarginSummaryCard(recipeList)
            Spacer(Modifier.height(24.dp))
        }

        // 레시피 섹션
        item {
            Text("레시피 관리", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
        }

        // 레시피 테이블 헤더
        item {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("레시피 이름", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("판매가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("원가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("마진", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }
            Divider(color = DividerGray, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
        }

        // 레시피 리스트
        items(recipeList) { recipe ->
            RecipeItem(
                name = recipe.title,
                price = recipe.price,
                cost = recipe.margin_info.total_ingredient_price.toInt(),
                margin = recipe.margin_info.margin.toInt(),
                marginRate = recipe.margin_info.margin_percentage.toInt(),
                onEditClick = { onEditRecipeClick(recipe.title) }
            )
            Spacer(Modifier.height(4.dp))
        }

        // 레시피 등록 버튼
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRegisterRecipeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("레시피 등록", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(32.dp))
        }

        // 재료 섹션
        item {
            Text("재료 관리", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
        }

        // 재료 테이블 헤더
        item {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("재료 이름", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("구매가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("사용량", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("원가", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }
            Divider(color = DividerGray, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
        }

        // 재료 리스트
        items(ingredients) { ingredient ->
            IngredientItem(
                name = ingredient.name,
                price = ingredient.price,
                usage = ingredient.grams.toInt(),
                onEditClick = { onEditIngredientClick(ingredient.name) }
            )
            Spacer(Modifier.height(4.dp))
        }

        // 재료 등록 버튼
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRegisterIngredientClick,
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("재료 등록", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun MarginSummaryCard(recipes: List<com.glowstudio.android.blindsjn.feature.foodcost.model.Recipe>) {
    val totalSales = recipes.sumOf { it.price }
    val totalCost = recipes.sumOf { it.margin_info.total_ingredient_price.toInt() }
    val totalMargin = totalSales - totalCost
    val marginRate = if (totalSales > 0) (totalMargin * 100f / totalSales).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("전체 마진 현황", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MarginStatItem("총 매출", totalSales)
                MarginStatItem("총 원가", totalCost)
                MarginStatItem("총 마진", totalMargin)
                MarginStatItem("마진율", marginRate, "%")
            }
        }
    }
}

@Composable
private fun MarginStatItem(label: String, value: Int, suffix: String = "원") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            "${String.format("%,d", value)}$suffix",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun RecipeItem(
    name: String,
    price: Int,
    cost: Int,
    margin: Int,
    marginRate: Int,
    onEditClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Text("%,d원".format(price), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Text("%,d원".format(cost), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Column(Modifier.weight(1.5f), horizontalAlignment = Alignment.Start) {
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(marginRate / 100f)
                        .background(Blue, RoundedCornerShape(6.dp))
                )
            }
            Spacer(Modifier.height(2.dp))
            Text("${margin}원 (${marginRate}%)", fontSize = 12.sp, color = Blue)
        }
        TextButton(onClick = onEditClick) {
            Text("수정", color = Blue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IngredientItem(
    name: String,
    price: Int,
    usage: Int,
    onEditClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Text("%,d원".format(price), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Text("%,d개".format(usage), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Text("%,d원".format(price * usage), Modifier.weight(1.5f), fontSize = 14.sp, color = TextPrimary)
        TextButton(onClick = onEditClick) {
            Text("수정", color = Blue, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodCostDashboardScreenPreview() {
    FoodCostDashboardScreen()
} 