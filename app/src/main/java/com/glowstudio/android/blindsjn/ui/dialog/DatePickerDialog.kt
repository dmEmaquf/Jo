package com.glowstudio.android.blindsjn.ui.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (selectedDate: LocalDate) -> Unit
) {
    // DatePickerState를 생성 (초기 선택 날짜: 현재 날짜)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // 선택한 날짜(밀리초)를 LocalDate로 변환
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null) {
                    val selectedDate = Instant.ofEpochMilli(selectedMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onConfirm(selectedDate)
                }
                onDismiss()
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        // 다이얼로그 내부에 DatePicker 배치
        DatePicker(state = datePickerState)
    }
}
