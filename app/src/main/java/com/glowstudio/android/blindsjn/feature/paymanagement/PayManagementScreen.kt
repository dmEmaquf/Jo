package com.glowstudio.android.blindsjn.feature.paymanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import com.glowstudio.android.blindsjn.ui.theme.*
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel.PayManagementViewModel

@Composable
fun PayManagementScreen(
    viewModel: PayManagementViewModel = hiltViewModel(),
    onNavigateToFoodCost: () -> Unit = {},
    onNavigateToSalesInput: () -> Unit = {},
) {
    val periodTabs = listOf("일", "주", "월", "연")
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val salesSummary by viewModel.salesSummary.collectAsState()
    val salesComparison by viewModel.salesComparison.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // 상단 탭 버튼
            item(key = "top_tabs") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TabButton(
                        text = "매출관리",
                        selected = true,
                        onClick = { /* 현재 화면 */ },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "마진관리",
                        selected = false,
                        onClick = onNavigateToFoodCost,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 기간 선택 탭
            item(key = "period_tabs") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periodTabs.forEach { period ->
                        PeriodTab(
                            text = period,
                            selected = period == selectedPeriod,
                            onClick = { viewModel.setPeriod(period) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 로딩 상태 표시
            if (isLoading) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 에러 상태 표시
            error?.let { errorMessage ->
                item(key = "error_message") {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 매출 요약 데이터 표시
            salesSummary?.let { summary ->
                item(key = "sales_summary_card") {
                    SalesSummaryCard(summary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 매출 비교 데이터 표시
            salesComparison?.let { comparison ->
                item(key = "sales_comparison_card") {
                    SalesComparisonCard(comparison)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // TODO: Add other sections like Goal, Costs/Profit, Expected Sales, Top Items here using item or items
            // Example for a static item:
            item(key = "goal_progress_card") {
                 // Implement Goal progress card here
                 Spacer(modifier = Modifier.height(16.dp))
            }

             item(key = "costs_profit_card") {
                 // Implement Costs/Profit card here
                 Spacer(modifier = Modifier.height(16.dp))
             }

             item(key = "expected_sales_card") {
                 // Implement Expected Sales card here
                 Spacer(modifier = Modifier.height(16.dp))
             }

             item(key = "top_items_section") {
                 // Implement Top Items section here
                 Spacer(modifier = Modifier.height(16.dp))
             }

              item(key = "item_proportion_section") {
                 // Implement Item Proportion section (Pie Chart) here
                 Spacer(modifier = Modifier.height(16.dp))
             }
        }
    }
}

@Composable
private fun PeriodTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Blue else Color.White,
        border = BorderStroke(1.dp, if (selected) Blue else Color.Gray)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = if (selected) Color.White else Color.Gray
        )
    }
}

@Composable
private fun SalesSummaryCard(summary: SalesSummaryResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("매출 요약", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            summary.summary?.let { summaryData ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("총 매출", fontSize = 14.sp, color = TextSecondary)
                        Text(
                            "₩ ${summaryData.totalSales.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text("총 마진", fontSize = 14.sp, color = TextSecondary)
                        Text(
                            "₩ ${summaryData.totalMargin.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text("마진율", fontSize = 14.sp, color = TextSecondary)
                        Text(
                            "${summaryData.marginRate.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("데이터 로드 중...")
                }
            }
        }
    }
}

@Composable
private fun SalesComparisonCard(comparison: SalesComparisonResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("매출 비교", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            comparison.comparisons?.forEach { (period, data) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(period, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (period == "day" && data.previousSales == 0.0 && data.currentSales > 0.0) {
                            // 전일 매출 0에서 증가한 경우 현재 매출 금액과 '신규' 표시
                            Text(
                                "₩ ${data.currentSales.toInt()} (신규)",
                                color = Color.Green,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            // 그 외 일반적인 경우 (증감률 표시)
                            Icon(
                                if (data.isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (data.isIncrease) Color.Green else Color.Red
                            )
                            Text(
                                "${data.differenceRate.toInt()}%",
                                color = if (data.isIncrease) Color.Green else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    proportions: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(140.dp)
    ) {
        var startAngle = -90f
        proportions.forEachIndexed { idx, proportion ->
            val sweep = proportion * 360f
            drawArc(
                color = colors[idx],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            // % 표기 (중앙 각도 계산)
            val angle = startAngle + sweep / 2
            val radius = size.minDimension / 2.5f
            val percent = (proportion * 100).toInt()
            val x = center.x + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = center.y + radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()

            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f
                    color = android.graphics.Color.BLACK
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    "$percent%",
                    x,
                    y + 10f,
                    paint
                )
            }
            startAngle += sweep
        }
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
fun SummaryStatItem(label: String, value: Int, suffix: String = "원") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            if (value >= 10000) {
                "${String.format("%.1f", value / 10000f)}만$suffix"
            } else {
                "${String.format("%,d", value)}$suffix"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PayManagementScreenPreview() {
    PayManagementScreen()
} 