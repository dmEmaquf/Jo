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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel.PayManagementViewModel
import com.glowstudio.android.blindsjn.feature.paymanagement.model.TopItem
import com.glowstudio.android.blindsjn.feature.paymanagement.model.TopItemsResponse
import java.time.LocalDate
import kotlin.collections.firstOrNull

@Composable
private fun GoalSettingDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var goalText by remember { mutableStateOf((currentGoal / 10000).toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("월간 매출 목표 설정") },
        text = {
            OutlinedTextField(
                value = goalText,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        goalText = it
                    }
                },
                label = { Text("목표 금액 (만원)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val goal = goalText.toDoubleOrNull() ?: 350.0
                    onConfirm(goal * 10000)
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

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
    val topItems by viewModel.topItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()
    val weeklyAverage by viewModel.weeklyAverage.collectAsState()
    val monthlyGoal by viewModel.monthlyGoal.collectAsState()
    val monthlyProgress by viewModel.monthlyProgress.collectAsState()
    val showGoalSettingDialog by viewModel.showGoalSettingDialog.collectAsState()

    // 요일별 마진율: [월, 화, 수, 목, 금, 토, 일] (평균 37%)
    val marginRates = listOf(35, 38, 36, 37, 39, 34, 40)
    val barLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // 상단 탭 버튼
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TabButton(text = "매출관리", selected = true, onClick = { /* 현재 화면 */ }, modifier = Modifier.weight(1f))
                    TabButton(text = "마진관리", selected = false, onClick = onNavigateToFoodCost, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 로딩 상태 표시
            if (isLoading) {
                item {
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
                item {
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

            // 1. 전체 매출 현황
            salesSummary?.let { summary ->
                item {
                    SalesSummaryCard(summary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 경고 배너
            item {
                salesComparison?.comparisons?.get("week")?.let { weekComparison ->
                    if (weekComparison.differenceRate != 0.0) {
                        Surface(
                            color = if (weekComparison.isIncrease) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (weekComparison.isIncrease) Icons.Default.ArrowUpward else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (weekComparison.isIncrease) Color(0xFF388E3C) else Color.Red
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "지난주 대비 매출이 ${if (weekComparison.isIncrease) "+" else ""}${weekComparison.differenceRate.toInt()}% ${if (weekComparison.isIncrease) "증가" else "감소"}했어요!",
                                    color = if (weekComparison.isIncrease) Color(0xFF388E3C) else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. 목표 달성률 ProgressBar
            item {
                Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이번달 매출 목표", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(
                                onClick = { viewModel.showGoalSettingDialog() }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "목표 설정",
                                    tint = TextSecondary
                                )
                            }
                        }
                        Text(
                            "${(monthlyGoal / 10000).toInt()}만원",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blue
                        )
                        Spacer(Modifier.height(8.dp))
                        val progress = (monthlyProgress / monthlyGoal).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Blue
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${(monthlyProgress / 10000).toInt()}만원 / ${(monthlyGoal / 10000).toInt()}만원 (${(progress * 100).toInt()}%)",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 3. 매출 추이 섹션
            item {
                SectionLayout(title = "매출 추이", onMoreClick = onNavigateToSalesInput) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // 막대그래프
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                val maxValue = weeklySales.maxOrNull()?.toFloat() ?: 500000f
                                val average = weeklyAverage.toFloat()
                                val compactAverage = if (average >= 10000) "${(average / 10000).toInt()}만" else average.toInt().toString()
                                val barHeightPx = 120f
                                val yOffsetPx = (1 - (average / maxValue)) * barHeightPx
                                val yOffset = yOffsetPx.dp
                                
                                // 평균선
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val y = size.height - ((average / maxValue) * (size.height - 120f)) - 40f
                                    drawLine(
                                        color = Color(0xFF4CAF50),  // 초록색으로 변경
                                        start = Offset(30f, y),
                                        end = Offset(size.width - 30f, y),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                                ) {
                                    weeklySales.forEachIndexed { idx, value ->
                                        val today = LocalDate.now()
                                        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                                        val currentDate = startOfWeek.plusDays(idx.toLong())
                                        val isToday = idx == today.dayOfWeek.value - 1
                                        val isFuture = currentDate.isAfter(today)
                                        val isDanger = marginRates[idx] < 20
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            // 금액 라벨 (compact)
                                            val compactValue = if (isFuture) "-" else if (value >= 10000) "${(value / 10000).toInt()}만" else value.toInt().toString()
                                            Text(
                                                compactValue,
                                                fontSize = 11.sp,
                                                color = when {
                                                    isToday -> Blue
                                                    isFuture -> TextSecondary
                                                    isDanger -> Color.Red
                                                    else -> TextHint
                                                },
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                            // 막대
                                            Box(
                                                modifier = Modifier
                                                    .width(32.dp)
                                                    .height(if (isFuture) 0.dp else (value / maxValue * barHeightPx).dp)
                                                    .background(
                                                        when {
                                                            isToday -> Blue
                                                            isFuture -> Color.Transparent
                                                            isDanger -> Color.Red
                                                            idx == 5 -> LightBlue // 토요일
                                                            idx == 6 -> Color(0xFFE0E0E0) // 일요일(연회색)
                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            )
                                            // 날짜
                                            Text(
                                                barLabels[idx],
                                                fontSize = 14.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isToday -> Blue
                                                    isFuture -> TextSecondary
                                                    else -> TextSecondary
                                                }
                                            )
                                        }
                                    }
                                }
                                // 평균선 라벨 - 왼쪽
                                Text(
                                    "평균",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 2.dp)
                                        .offset(y = yOffset+8.dp)
                                )
                                // 평균선 라벨 - 오른쪽
                                Text(
                                    compactAverage,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 2.dp)
                                        .offset(y = yOffset+8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. 고정비/순이익 카드
            item {
                Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("이번달 고정비/순이익", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("고정비", fontSize = 14.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("₩ 1,200,000", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("순이익", fontSize = 14.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("₩ 350,000", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Blue)
                            }
                        }
                    }
                }
            }

            // 5. 예상 매출 카드
            item {
                val dailyAverage by viewModel.dailyAverage.collectAsState()
                val dailyComparison by viewModel.dailyComparison.collectAsState()
                val todaySales = salesSummary?.summary?.totalSales ?: 0.0
                
                Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("오늘의 매출", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "₩ ${String.format("%,d", todaySales.toInt())}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue
                        )
                        Text(
                            "다른 요일 대비 ${if (dailyComparison >= 0) "+" else ""}${dailyComparison.toInt()}%",
                            color = if (dailyComparison >= 0) Color(0xFF388E3C) else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 매출 TOP3 섹션
            topItems?.let { items ->
                item {
                    SectionLayout(title = "매출 TOP3", onMoreClick = null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                items.topItems?.forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${idx + 1}위", color = Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.recipeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    if (idx != (items.topItems?.size ?: 0) - 1) {
                                        Divider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 품목별 비중 섹션
            topItems?.let { items ->
                item {
                    SectionLayout(title = "품목별 비중", onMoreClick = null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // 기간 선택 탭 (일/주/월/연)
                                Row {
                                    periodTabs.forEachIndexed { idx, period ->
                                        TextButton(
                                            onClick = { viewModel.setPeriod(period) },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = if (selectedPeriod == period) Blue else TextSecondary
                                            ),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .width(36.dp)
                                        ) {
                                            Text(period, fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal)
                                        }
                                        if (idx != periodTabs.lastIndex) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // 원그래프 + 범례
                                items.topItems?.let { topItems ->
                                    val totalSales = topItems.sumOf { it.totalSales }
                                    val proportions = topItems.map { (it.totalSales / totalSales).toFloat() }
                                    val colors = listOf(LightBlue, Color(0xFFB3E5FC), Color(0xFF81D4FA), Color(0xFF4FC3F7))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .height(200.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(140.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            PieChart(
                                                proportions = proportions,
                                                colors = colors
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(24.dp))
                                        Column {
                                            topItems.forEachIndexed { idx, item ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .background(colors[idx], RoundedCornerShape(10.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(item.recipeName, fontSize = 15.sp, color = TextPrimary)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 매출 비교 카드
            salesComparison?.let { comparison ->
                item {
                    SalesComparisonCard(comparison)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 목표 설정 다이얼로그를 Box의 자식으로 이동
        if (showGoalSettingDialog) {
            GoalSettingDialog(
                currentGoal = monthlyGoal,
                onDismiss = { viewModel.hideGoalSettingDialog() },
                onConfirm = { goal -> 
                    viewModel.setMonthlyGoal(goal)
                    viewModel.hideGoalSettingDialog()
                }
            )
        }

        // 플로팅 액션 버튼
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onNavigateToSalesInput,
                containerColor = Blue,
                contentColor = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "오늘의 매출입력")
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
fun SalesSummaryCard(summary: SalesSummaryResponse) {
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
                Text("전체 매출 현황", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            summary.summary?.let { summaryData ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStatItem("총 매출", summaryData.totalSales.toInt())
                    SummaryStatItem("총 마진", summaryData.totalMargin.toInt())
                    SummaryStatItem("마진율", summaryData.marginRate.toInt(), "%")
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
fun SummaryStatItem(label: String, value: Int, suffix: String = "원") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            if (value >= 10000) {
                "${(value / 10000).toInt()}만$suffix"
            } else {
                "${value.toInt()}$suffix"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun SalesComparisonCard(comparison: SalesComparisonResponse) {
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
                    Text(
                        when (period) {
                            "day" -> "전일 대비"
                            "week" -> "전주 대비"
                            "month" -> "전월 대비"
                            "year" -> "전년 대비"
                            else -> period
                        },
                        fontSize = 14.sp
                    )
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

@Preview(showBackground = true)
@Composable
fun PayManagementScreenPreview() {
    PayManagementScreen()
} 