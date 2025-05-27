package com.glowstudio.android.blindsjn.feature.calendar
// 기존에 메시지로 만들려던 것을 캘린더로 바꾼거라 이름이 이따구임. 헷갈리지 말 것.
// 루트 값 또한 message로 작용하고 있다.

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import androidx.navigation.NavController
import com.glowstudio.android.blindsjn.ui.theme.BackgroundWhite

/**
 * 이벤트 데이터 클래스
 * date: 이벤트가 발생하는 날짜
 * title: 이벤트 제목
 * time: 이벤트 시간
 */
data class CalendarEvent(
    val date: LocalDate,
    val title: String,
    val time: String
)

/**
 * 캘린더 화면
 */
@Composable
fun MessageScreen(navController: NavController) {
    // 현재 월/연도 상태
    val currentYearMonth = remember { mutableStateOf(YearMonth.now()) }

    // 선택된 날짜 상태
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    // 이벤트 더미 데이터
    val events = listOf(
        CalendarEvent(LocalDate.now(), "회의", "09:00"),
        CalendarEvent(LocalDate.now().plusDays(1), "점심 약속", "12:00"),
        CalendarEvent(LocalDate.now().plusDays(3), "스터디", "19:00")
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        "${currentYearMonth.value.year}년 ${currentYearMonth.value.month.value}월",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = {
                        // 이전 달로 이동
                        currentYearMonth.value = currentYearMonth.value.minusMonths(1)
                    }) {
                        Icon(Icons.Filled.ArrowBackIos, contentDescription = "이전 달")
                    }
                    IconButton(onClick = {
                        // 다음 달로 이동
                        currentYearMonth.value = currentYearMonth.value.plusMonths(1)
                    }) {
                        Icon(Icons.Filled.ArrowForwardIos, contentDescription = "다음 달")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 요일 헤더
                DaysOfWeekHeader()

                // 달력 그리드
                val days = generateCalendarDates(currentYearMonth.value)
                CalendarGrid(
                    days = days,
                    selectedDate = selectedDate.value,
                    onDayClick = { date ->
                        selectedDate.value = date
                    }
                )

                // 선택된 날짜의 이벤트 목록
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedDate.value?.toString() ?: "날짜를 선택하세요",
                    style = MaterialTheme.typography.titleMedium
                )
                EventList(
                    events = events.filter { it.date == selectedDate.value }
                )
            }
        },
        floatingActionButton = {
            // 일정 추가 화면으로 이동
            FloatingActionButton(
                onClick = {
                    navController.navigate("addSchedule")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "일정 추가")
            }
        }
    )
}

// 요일 헤더 (일, 월, 화, 수, 목, 금, 토)
@Composable
fun DaysOfWeekHeader() {
    val dayOfWeeks = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayOfWeeks.forEach { day ->
            Text(text = day, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// 6주(42일) 분량의 날짜를 7칸씩 표시
@Composable
fun CalendarGrid(
    days: List<LocalDate>,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    // 6줄 (주) * 7칸 (요일)
    val rows = days.chunked(7)
    Column {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    CalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDayClick(date) }
                    )
                }
            }
        }
    }
}

/**
 * 날짜 셀
 */
@Composable
fun RowScope.CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f) // 정사각형
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 선택된 날짜 강조
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

/**
 * 날짜 목록(42일)을 생성하는 함수
 */
fun generateCalendarDates(yearMonth: YearMonth): List<LocalDate> {
    // 해당 달의 첫 번째 날짜
    val firstDayOfMonth = yearMonth.atDay(1)
    // 해당 달의 마지막 날짜
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // 달력에서 첫 주가 시작되는 일요일(또는 원하는 기준 요일)
    val startOfCalendar = firstDayOfMonth.with(
        java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
    )
    // 달력에서 마지막 주가 끝나는 토요일(또는 원하는 기준 요일)
    val endOfCalendar = lastDayOfMonth.with(
        java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)
    )

    // startOfCalendar부터 endOfCalendar까지 모든 날짜를 리스트로 생성
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startOfCalendar, endOfCalendar) + 1
    return (0 until totalDays).map { startOfCalendar.plusDays(it) }
}

/**
 * 이벤트 목록
 */
@Composable
fun EventList(events: List<CalendarEvent>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(events) { event ->
            EventItem(event)
        }
    }
}

/**
 * 이벤트 아이템
 */
@Composable
fun EventItem(event: CalendarEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = event.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
