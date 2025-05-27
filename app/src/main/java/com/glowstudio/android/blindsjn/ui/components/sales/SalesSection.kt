package com.glowstudio.android.blindsjn.ui.components.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.components.common.SectionLayout

@Composable
fun SalesSection() {
    SectionLayout(title = "오늘의 매출 관리") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
           // 여기에 뭐 넣을지 고민 좀 해봐야 함
        }
    }
} 