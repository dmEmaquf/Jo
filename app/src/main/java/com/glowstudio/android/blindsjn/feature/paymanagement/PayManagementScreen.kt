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

@Composable
fun PayManagementScreen(
    onNavigateToFoodCost: () -> Unit = {},
    onNavigateToSalesInput: () -> Unit = {},
) {
    val periodTabs = listOf("일", "주", "월", "연")
    var selectedPeriod by remember { mutableStateOf("일") }

    // 샘플 데이터 (7일치)
    val barData = listOf(350000, 380000, 320000, 400000, 450000, 500000, 420000) // 월~일
    val barLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    val pieData = listOf(0.45f, 0.25f, 0.20f, 0.10f) // 떡볶이, 김밥, 튀김, 음료
    val pieLabels = listOf("떡볶이", "김밥", "튀김", "음료")
    val pieColors = listOf(LightBlue, Color(0xFFB3E5FC), Color(0xFF81D4FA), Color(0xFF4FC3F7))

    // 하드코딩: 전일 대비 증감률, 마진 위험, TOP3 데이터
    val salesDiff = 8 // +8% 증가
    // 요일별 마진율: [월, 화, 수, 목, 금, 토, 일] (평균 37%)
    val marginRates = listOf(35, 38, 36, 37, 39, 34, 40)
    val top3List = listOf(
        Triple("떡볶이", 157500, 39),
        Triple("김밥", 75000, 36),
        Triple("튀김", 50000, 34)
    )

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
            // 1. 전체 매출 현황
            item {
                SalesSummaryCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
            // 경고 배너
            item {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("지난주 대비 매출이 4% 감소했어요!", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // 2. 목표 달성률 ProgressBar
            item {
                Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(Modifier.padding(16.dp)) {
                        val progress = 0.75f // 75% 달성
                        Text("이번달 매출 목표: 350만원", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = Blue)
                        Spacer(Modifier.height(4.dp))
                        Text("262.5만원 / 350만원 (75%)", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            // 3. 매출 추이 섹션 (이동)
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
                                val maxValue = 500000f // 토요일 매출을 최대값으로 고정
                                val goal = 450000
                                val compactGoal = if (goal >= 10000) "${goal / 10000}만" else goal.toString()
                                val barHeightPx = 120f // same as used for bar height
                                val yOffsetPx = (1 - (goal.toFloat() / maxValue)) * barHeightPx
                                val yOffset = yOffsetPx.dp
                                // 목표선
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val y = size.height - ((goal.toFloat() / maxValue) * (size.height - 120f)) - 40f
                                    drawLine(
                                        color = Color.Red,
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
                                    barData.forEachIndexed { idx, value ->
                                        val isToday = idx == barData.lastIndex
                                        val isDanger = marginRates[idx] < 20
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            // 금액 라벨 (compact)
                                            val compactValue = if (value >= 10000) String.format("%.1f만", value / 10000f) else value.toString()
                                            Text(
                                                compactValue,
                                                fontSize = 11.sp,
                                                color = if (isToday) Blue else if (isDanger) Color.Red else TextHint,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                            // 막대
                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .height((value / maxValue * barHeightPx).dp)
                                                    .background(
                                                        when {
                                                            isToday -> Blue
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
                                                color = if (isToday) Blue else TextSecondary
                                            )
                                        }
                                    }
                                }
                                // 목표선 라벨 - 왼쪽
                                Text(
                                    "목표",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 2.dp)
                                        .offset(y = yOffset+8.dp)
                                )
                                // 목표선 라벨 - 오른쪽
                                Text(
                                    compactGoal,
                                    color = Color.Red,
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
                Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("금요일 평균 매출", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("₩ 450,000", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Blue)
                        Text("다른 요일 대비 +25%", color = Color(0xFF388E3C), fontSize = 14.sp)
                    }
                }
            }
            // 매출 TOP3 섹션
            item {
                SectionLayout(title = "매출 TOP3",
                    onMoreClick = null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            top3List.forEachIndexed { idx, item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${idx + 1}위", color = Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(item.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.weight(1f))
                                    Text("${item.second}원", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                if (idx != top3List.lastIndex) {
                                    Divider(color = DividerGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
            // 품목별 비중 섹션
            item {
                SectionLayout(title = "품목별 비중",
                    onMoreClick = null) {
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
                                        onClick = { selectedPeriod = period },
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
                                        proportions = pieData,
                                        colors = pieColors
                                    )
                                }
                                Spacer(modifier = Modifier.width(24.dp))
                                Column {
                                    pieLabels.forEachIndexed { idx, label ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(pieColors[idx], RoundedCornerShape(10.dp))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(label, fontSize = 15.sp, color = TextPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // 비율 표시 (생략)
                        }
                    }
                }
            }
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
fun SalesSummaryCard() {
    // 하드코딩 예시 데이터
    val totalSales = 350000
    val totalCost = 210000
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
                Text("전체 매출 현황", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary, modifier = Modifier.weight(1f))
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
                SummaryStatItem("총 매출", totalSales)
                SummaryStatItem("총 원가", totalCost)
                SummaryStatItem("총 마진", totalMargin)
                SummaryStatItem("마진율", marginRate, "%")
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