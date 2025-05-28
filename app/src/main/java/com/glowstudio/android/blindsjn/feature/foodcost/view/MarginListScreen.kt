package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.MarginViewModel
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecentSale

@Composable
fun MarginListScreen(
    onRecipeListClick: () -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {},
    onIngredientListClick: () -> Unit = {},
    onRegisterIngredientClick: () -> Unit = {},
    onNavigateToPayManagement: () -> Unit = {},
    onNavigateToMargin: () -> Unit = {},
) {
    val viewModel: MarginViewModel = viewModel()
    val marginData by viewModel.marginData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMarginData(1) // TODO: 실제 앱에서는 로그인 정보에서 business_id 받아야 함
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        // 상단 탭
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton(text = "매출관리", selected = false, onClick = onNavigateToPayManagement, modifier = Modifier.weight(1f))
            TabButton(text = "마진관리", selected = true, onClick = onNavigateToMargin, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error ?: "오류가 발생했습니다.", color = Color.Red)
            }
        } else {
            // 요약 정보 표시
            marginData?.summary?.let { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("전체 요약", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryItem("총 레시피", "${summary.total_recipes}개")
                            SummaryItem("총 매출", "${summary.total_sales}원")
                            SummaryItem("총 원가", "${summary.total_cost}원")
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryItem("총 마진", "${summary.total_margin}원")
                            SummaryItem("평균 마진율", "${summary.avg_margin_percentage}%")
                        }
                    }
                }
            }

            // 표 헤더
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(4.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text("이름", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("판매가", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("원가", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("마진", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("마진율", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }

            // 표 리스트
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(2.dp, Blue, RoundedCornerShape(4.dp))
                    .background(Color.White)
            ) {
                Column(Modifier.fillMaxSize()) {
                    marginData?.recent_sales?.forEach { sale ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(2.dp))
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(sale.title, modifier = Modifier.weight(1f))
                            Text("${sale.price}원", modifier = Modifier.weight(1f))
                            Text("${sale.total_ingredient_price}원", modifier = Modifier.weight(1f))
                            Text("${sale.margin.toInt()}원", modifier = Modifier.weight(1f))
                            val marginPercentage = if (sale.price > 0) {
                                ((sale.margin.toDouble() / sale.price) * 100).toInt()
                            } else 0
                            Text("${marginPercentage}%", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        // 하단 버튼들
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MainButton("레시피 리스트", onRecipeListClick)
                Spacer(Modifier.height(12.dp))
                MainButton("재료 리스트", onIngredientListClick)
            }
            Column(modifier = Modifier.weight(1f)) {
                MainButton("레시피 등록", onRegisterRecipeClick)
                Spacer(Modifier.height(12.dp))
                MainButton("재료 등록", onRegisterIngredientClick)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
fun ChartTabButton(text: String, selected: Boolean) {
    Box(
        Modifier
            .padding(end = 8.dp)
            .background(if (selected) Blue else Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Blue, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text, color = if (selected) Color.White else Blue, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BarChartBar(label: String, percent: Float, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(32.dp)
                .height((100 * percent).dp)
                .background(Blue, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text("$value", color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun PieChart(percent1: Float, percent2: Float) {
    // 실제 파이차트 대신 원형 Box 2개로 대체 (예시)
    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(80.dp)
                .background(LightBlue, shape = RoundedCornerShape(40.dp))
        )
        Box(
            Modifier
                .size(80.dp * percent1)
                .background(Blue, shape = RoundedCornerShape(40.dp))
        )
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

data class MarginItem(
    val name: String,
    val price: Int,
    val cost: Int,
    val margin: Int,
    val marginRate: Int
)

@Preview(showBackground = true)
@Composable
fun MarginListScreenPreview() {
    MarginListScreen()
} 