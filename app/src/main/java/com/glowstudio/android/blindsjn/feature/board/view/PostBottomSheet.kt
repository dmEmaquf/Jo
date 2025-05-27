package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostBottomSheet(
    tags: List<String>,
    enabledTags: List<String> = tags,
    onDone: (List<String>) -> Unit
) {
    val selectedTags = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("태그를 선택하세요", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val enabled = enabledTags.contains(tag)
                val selected = selectedTags.contains(tag)
                TagChip(
                    text = tag,
                    selected = selected,
                    enabled = enabled,
                    onClick = {
                        if (enabled) {
                            if (selected) selectedTags.remove(tag)  
                            else selectedTags.add(tag)
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        CommonButton(
            text = "작성하기",
            onClick = { onDone(selectedTags.toList()) },
            enabled = selectedTags.isNotEmpty()
        )
    }
}

@Composable
fun TagChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        selected -> Color.White
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = if (!selected && enabled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostBottomSheetPreview() {
    val tags = listOf("누구나", "재학생", "패션디자인", "비활성1", "비활성2")
    val enabledTags = listOf("누구나", "재학생", "패션디자인")
    PostBottomSheet(
        tags = tags,
        enabledTags = enabledTags,
        onDone = {}
    )
} 