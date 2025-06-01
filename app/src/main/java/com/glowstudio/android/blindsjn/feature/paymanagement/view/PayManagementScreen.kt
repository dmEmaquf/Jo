package com.glowstudio.android.blindsjn.feature.paymanagement.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.lazy.LazyColumn
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.KeyboardType
import com.glowstudio.android.blindsjn.data.network.Network
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparison
import com.glowstudio.android.blindsjn.feature.paymanagement.repository.PayManagementApi
import com.glowstudio.android.blindsjn.feature.paymanagement.repository.PayManagementRepository
import com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel.PayManagementViewModel
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlinx.coroutines.delay
import androidx.compose.material3.ModalBottomSheet
import com.glowstudio.android.blindsjn.feature.ocr.view.DailySalesBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextAlign

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
private fun FixedCostSettingDialog(
    currentCost: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var costText by remember { mutableStateOf((currentCost / 10000).toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("월간 고정비 설정") },
        text = {
            OutlinedTextField(
                value = costText,
                onValueChange = { 
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        costText = it
                    }
                },
                label = { Text("고정비 금액 (만원)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cost = costText.toDoubleOrNull() ?: 120.0
                    onConfirm(cost * 10000)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayManagementScreen(
    viewModel: PayManagementViewModel,
    onNavigateToFoodCost: () -> Unit,
    onNavigateToSalesInput: () -> Unit,
    onNavigateToOcr: () -> Unit,
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

    // 애니메이션 시작을 위한 상태
    var startAnimation by remember { mutableStateOf(false) }
    
    // 화면이 로드된 후 애니메이션 시작
    LaunchedEffect(Unit) {
        delay(100) // 화면이 완전히 로드될 때까지 잠시 대기
        startAnimation = true
    }

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
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1. 전체 매출 현황
            salesSummary?.let { summary ->
                item {
                    SalesSummaryCard(summary)
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
                                    tint = if (weekComparison.isIncrease) Color(0xFF388E3C) else Color(0xFFE53935)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "지난주 대비 매출이 ${if (weekComparison.isIncrease) "+" else ""}${weekComparison.differenceRate.toInt()}% ${if (weekComparison.isIncrease) "증가" else "감소"}했어요!",
                                    color = if (weekComparison.isIncrease) Color(0xFF388E3C) else Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. 목표 달성률 ProgressBar
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이번달 매출 목표", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                        val progress = (monthlyProgress / monthlyGoal).toFloat().coerceIn(0f, 1f)
                        val animatedProgress by animateFloatAsState(
                            targetValue = if (!startAnimation) 0f else progress,
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                            label = "goalProgress"
                        )
                        Text(
                            "${(animatedProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Blue
                        )
                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Blue
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${(monthlyProgress / 10000).toInt()}만원 / ${(monthlyGoal / 10000).toInt()}만원",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }

            // 3. 매출 추이 섹션
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("매출 추이", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
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
                                    
                                    val animatedHeight by animateFloatAsState(
                                        targetValue = if (!startAnimation) 0f else if (isFuture) 0f else (value.toFloat() / maxValue * barHeightPx),
                                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                                        label = "barHeight"
                                    )
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // 금액 라벨 (compact)
                                        val compactValue = if (isFuture) "-" else if (value >= 10000) "${(value / 10000).toInt()}만" else value.toInt().toString()
                                        Text(
                                            compactValue,
                                            fontSize = 11.sp,
                                            color = when {
                                                isToday -> Blue
                                                isFuture -> TextSecondary
                                                isDanger -> Color(0xFFE53935)
                                                else -> TextHint
                                            },
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        // 막대
                                        Box(
                                            modifier = Modifier
                                                .width(32.dp)
                                                .height(animatedHeight.dp)
                                                .background(
                                                    when {
                                                        isToday -> Blue
                                                        isFuture -> Color.Transparent
                                                        isDanger -> Color(0xFFE53935)
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
                        
                        // 컴팩트 비교 섹션 추가
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = DividerGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 비교 데이터 표시
                        salesComparison?.comparisons?.let { comparisons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ComparisonItem(
                                    label = "전일",
                                    data = comparisons["day"],
                                    modifier = Modifier.weight(1f)
                                )
                                ComparisonItem(
                                    label = "전주",
                                    data = comparisons["week"],
                                    modifier = Modifier.weight(1f)
                                )
                                ComparisonItem(
                                    label = "전월",
                                    data = comparisons["month"],
                                    modifier = Modifier.weight(1f)
                                )
                                ComparisonItem(
                                    label = "전년",
                                    data = comparisons["year"],
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. 고정비/순이익 카드
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이번달 고정비/순이익", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            IconButton(
                                onClick = { viewModel.showFixedCostSettingDialog() }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "고정비 설정",
                                    tint = TextSecondary
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("고정비", fontSize = 14.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "₩ ${String.format("%,d", viewModel.fixedCost.collectAsState().value.toInt())}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("순이익", fontSize = 14.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("₩ 350,000", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Blue)
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
                
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("오늘의 매출", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "₩ ${String.format("%,d", todaySales.toInt())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue
                        )
                        Text(
                            "다른 요일 대비 ${if (dailyComparison >= 0) "+" else ""}${dailyComparison.toInt()}%",
                            color = if (dailyComparison >= 0) Color(0xFF388E3C) else Color(0xFFE53935),
                            fontSize = 14.sp
                        )
                    }
                }
            }

//            // 매출 TOP3 섹션
//            topItems?.let { items ->
//                item {
//                    SectionLayout(title = "매출 TOP3", onMoreClick = null) {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(20.dp),
//                            colors = CardDefaults.cardColors(containerColor = CardWhite)
//                        ) {
//                            Column(Modifier.padding(16.dp)) {
//                                items.topItems?.forEachIndexed { idx, item ->
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Text("${idx + 1}위", color = Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                                        Spacer(Modifier.width(8.dp))
//                                        Text(item.recipeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                                        }
//                                    if (idx != (items.topItems?.size ?: 0) - 1) {
//                                        Divider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
//
//            // 품목별 비중 섹션
//            topItems?.let { items ->
//                item {
//                    SectionLayout(title = "품목별 비중", onMoreClick = null) {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(20.dp),
//                            colors = CardDefaults.cardColors(containerColor = CardWhite)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(16.dp)
//                            ) {
//                                // 기간 선택 탭 (일/주/월/연)
//                                Row {
//                                    periodTabs.forEachIndexed { idx, period ->
//                                        TextButton(
//                                            onClick = { viewModel.setPeriod(period) },
//                                            colors = ButtonDefaults.textButtonColors(
//                                                contentColor = if (selectedPeriod == period) Blue else TextSecondary
//                                            ),
//                                            modifier = Modifier
//                                                .height(32.dp)
//                                                .width(36.dp)
//                                        ) {
//                                            Text(period, fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal)
//                                        }
//                                        if (idx != periodTabs.lastIndex) {
//                                            Spacer(modifier = Modifier.width(2.dp))
//                                        }
//                                    }
//                                }
//                                Spacer(modifier = Modifier.height(8.dp))
//                                // 원그래프 + 범례
//                                items.topItems?.let { topItems ->
//                                    val totalSales = topItems.sumOf { it.totalSales }
//                                    val proportions = topItems.map { (it.totalSales / totalSales).toFloat() }
//                                    val colors = listOf(LightBlue, Color(0xFFB3E5FC), Color(0xFF81D4FA), Color(0xFF4FC3F7))
//
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth()
//                                            .height(200.dp),
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Box(
//                                            modifier = Modifier.size(140.dp),
//                                            contentAlignment = Alignment.Center
//                                        ) {
//                                            PieChart(
//                                                proportions = proportions,
//                                                colors = colors,
//                                                startAnimation = startAnimation
//                                            )
//                                        }
//                                        Spacer(modifier = Modifier.width(24.dp))
//                                        Column {
//                                            topItems.forEachIndexed { idx, item ->
//                                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                                    Box(
//                                                        modifier = Modifier
//                                                            .size(20.dp)
//                                                            .background(colors[idx], RoundedCornerShape(10.dp))
//                                                    )
//                                                    Spacer(modifier = Modifier.width(8.dp))
//                                                    Text(item.recipeName, fontSize = 15.sp, color = TextPrimary)
//                                                }
//                                                Spacer(modifier = Modifier.height(8.dp))
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }

//            // 매출 비교 카드
//            salesComparison?.let { comparison ->
//                item {
//                    SalesComparisonCard(comparison)
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
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

        // 고정비 설정 다이얼로그 추가
        if (viewModel.showFixedCostSettingDialog.collectAsState().value) {
            FixedCostSettingDialog(
                currentCost = viewModel.fixedCost.collectAsState().value,
                onDismiss = { viewModel.hideFixedCostSettingDialog() },
                onConfirm = { cost ->
                    viewModel.setFixedCost(cost)
                    viewModel.hideFixedCostSettingDialog()
                }
            )
        }

        // 플로팅 액션 버튼
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            var expanded by remember { mutableStateOf(false) }
            var showBottomSheet by remember { mutableStateOf(false) }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(16.dp)
            ) {
                if (expanded) {
                    Card(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .width(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            DropdownMenuItem(
                                text = { Text("OCR로 매출 입력") },
                                onClick = {
                                    onNavigateToOcr()
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("수동으로 매출 입력") },
                                onClick = {
                                    showBottomSheet = true
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                        }
                    }
                }
                
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = Blue,
                    contentColor = Color.White
                ) {
                    Icon(
                        if (expanded) Icons.Default.ArrowUpward else Icons.Default.Edit,
                        contentDescription = "매출 입력"
                    )
                }
            }
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    containerColor = Color.White
                ) {
                    DailySalesBottomSheet(
                        onDismiss = { showBottomSheet = false },
                        onSaved = { showBottomSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    proportions: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    startAnimation: Boolean
) {
    val animatedProportions = proportions.map { proportion ->
        animateFloatAsState(
            targetValue = if (!startAnimation) 0f else proportion,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            label = "pieProportion"
        )
    }

    Canvas(
        modifier = modifier.size(140.dp)
    ) {
        var startAngle = -90f
        animatedProportions.forEachIndexed { idx, animatedProportion ->
            val sweep = animatedProportion.value * 360f
            drawArc(
                color = colors[idx],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            // % 표기 (중앙 각도 계산)
            val angle = startAngle + sweep / 2
            val radius = size.minDimension / 2.5f
            val percent = (animatedProportion.value * 100).toInt()
            val x = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("전체 매출 현황", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, modifier = Modifier.weight(1f))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "매출 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            
            comparison.comparisons?.entries?.forEach { (period, data) ->
                val isIncrease = data.isIncrease
                val iconColor = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                val backgroundColor = if (isIncrease) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                val label = when (period) {
                    "day" -> "전일 대비"
                    "week" -> "전주 대비"
                    "month" -> "전월 대비"
                    "year" -> "전년 대비"
                    else -> period
                }
                val diffRate = data.differenceRate.toInt()
                val diffRateText = if (diffRate > 0) "+$diffRate%" else "$diffRate%"
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                label,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    diffRateText,
                                    color = iconColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        
                        // 원형 프로그레스 바
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                diffRateText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonItem(
    label: String,
    data: SalesComparison?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (data != null) {
            val isIncrease = data.isIncrease
            val iconColor = if (isIncrease) Color(0xFF4CAF50) else Color(0xFFD32F2F)
            val diffRate = data.differenceRate.toInt()
            val diffRateText = if (diffRate > 0) "+$diffRate%" else "$diffRate%"
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    diffRateText,
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            Text(
                "-",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayManagementScreenPreview() {
    val context = LocalContext.current
    val viewModel = remember {
        val api = Network.apiService as PayManagementApi
        val repository = PayManagementRepository(api, context)
        PayManagementViewModel(repository)
    }
    
    PayManagementScreen(
        viewModel = viewModel,
        onNavigateToFoodCost = {},
        onNavigateToSalesInput = {},
        onNavigateToOcr = {}
    )
} 