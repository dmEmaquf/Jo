package com.glowstudio.android.blindsjn.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipSelectedPreview() {
    BlindSJNTheme {
        FilterChip(
            text = "선택됨",
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipUnselectedPreview() {
    BlindSJNTheme {
        FilterChip(
            text = "선택안됨",
            isSelected = false,
            onClick = {}
        )
    }
}

