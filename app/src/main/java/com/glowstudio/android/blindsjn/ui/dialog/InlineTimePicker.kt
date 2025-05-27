package com.glowstudio.android.blindsjn.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineTimePicker(
    initialHour: Int = 13,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    // TimePicker 상태 생성
    val timePickerState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 인라인 TimePicker 표시
        TimePicker(state = timePickerState)
        // 선택 완료 버튼
        CommonButton(
            text = "확인",
            onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) },
            modifier = Modifier.align(Alignment.End)
        )
    }
}
