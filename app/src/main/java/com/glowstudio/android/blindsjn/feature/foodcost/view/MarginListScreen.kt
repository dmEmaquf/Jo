package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.feature.foodcost.model.MarginItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.MarginListViewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.IngredientViewModel

@Composable
fun MarginListScreen(
    onRecipeListClick: () -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {},
    onEditRecipeClick: (String) -> Unit = {},
    onEditIngredientClick: (String) -> Unit = {},
    onRegisterIngredientClick: () -> Unit = {},
    onIngredientListClick: () -> Unit = {},
    onNavigateToPayManagement: () -> Unit = {},
    onNavigateToMargin: () -> Unit = {}
) {
    val viewModel: MarginListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val items by viewModel.items.collectAsState()
    val ingredientViewModel: IngredientViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val ingredients by ingredientViewModel.ingredients.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadMarginData(1) // businessId는 실제 값으로 대체 필요
        ingredientViewModel.loadIngredients()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        // 상단 탭
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabButton(text = "매출관리", selected = false, onClick = onNavigateToPayManagement, modifier = Modifier.weight(1f))
                TabButton(text = "마진관리", selected = true, onClick = onNavigateToMargin, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }

        // 상단 마진 요약 카드
        item {
            MarginSummaryCard(items)
            Spacer(Modifier.height(24.dp))
        }

        // 레시피 섹션
        item {
            SectionLayout(
                title = "레시피 관리",
                onMoreClick = onRecipeListClick
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 레시피 테이블 헤더
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이름", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.Start)
                            Text("판매가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
                            Text("원가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
                            Text("마진", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
                        }
                        Divider(color = DividerGray, thickness = 1.dp)
                        Spacer(Modifier.height(8.dp))
                        // 레시피 리스트
                        items.forEachIndexed { idx, item ->
                            val margin = item.price - item.cost
                            val marginRate = item.marginRate
                            RecipeItem(
                                name = item.name,
                                price = item.price,
                                cost = item.cost,
                                margin = margin,
                                marginRate = marginRate
                            )
                            if (idx != items.lastIndex) {
                                Divider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // 재료 섹션
        item {
            SectionLayout(
                title = "재료 관리",
                onMoreClick = onIngredientListClick
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 재료 테이블 헤더
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이름", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.Start)
                            Text("단위(g)", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
                            Text("구매가", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
                        }
                        Divider(color = DividerGray, thickness = 1.dp)
                        Spacer(Modifier.height(8.dp))
                        // 재료 리스트
                        ingredients.forEachIndexed { idx, ingredient ->
                            IngredientItem(
                                name = ingredient.name,
                                grams = ingredient.grams,
                                price = ingredient.price
                            )
                            if (idx != ingredients.lastIndex) {
                                Divider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarginSummaryCard(recipes: List<MarginItem>) {
    val totalSales = recipes.sumOf { it.price }
    val totalCost = recipes.sumOf { it.cost }
    val totalMargin = totalSales - totalCost
    val marginRate = if (totalSales > 0) (totalMargin * 100f / totalSales).toInt() else 0

    var selectedPeriod by remember { mutableStateOf("일") }
    val periods = listOf("일", "주", "월")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("전체 마진 현황", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                Row {
                    periods.forEachIndexed { idx, period ->
                        TextButton(
                            onClick = { selectedPeriod = period },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (selectedPeriod == period) Blue else TextSecondary
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .width(36.dp),
                        ) {
                            Text(period, fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal)
                        }
                        if (idx != periods.lastIndex) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
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
    marginRate: Int
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.Start)
        Text("%,d원".format(price), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
        Text("%,d원".format(cost), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
        Column(Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
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
            Text("${margin}원 (${marginRate}%)", fontSize = 12.sp, color = Blue, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun IngredientItem(
    name: String,
    grams: Double,
    price: Int
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.Start)
        Text(String.format("%.1f", grams), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
        Text("%,d원".format(price), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.End)
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Blue else Color.White,
            contentColor = if (selected) Color.White else Blue
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun MarginBubble(
    name: String,
    sales: Int,
    cost: Int,
    marginRate: Int,
    color: Color,
    marginColor: Color,
    modifier: Modifier = Modifier
) {
    // 버블 크기 축소: 기본값 60, 매출에 따라 최대 50까지 증가
    val bubbleSize = (60 + (sales / 30000).coerceAtMost(50)).dp
    val marginRatio = marginRate / 100f
    Box(
        modifier = modifier.size(bubbleSize),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val R = size.minDimension / 2f
            val r = R * marginRatio
            drawCircle(
                color = color,
                radius = R,
                center = Offset(R, R)
            )
            drawCircle(
                color = marginColor,
                radius = r,
                center = Offset(R, R + (R - r))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(1.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Text("${sales}원", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarginListScreenPreview() {
    MarginListScreen()
} 