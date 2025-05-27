package com.glowstudio.android.blindsjn.feature.board.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryBottomSheet(
    categories: List<BoardCategory>,
    selectedCategory: BoardCategory?,
    onCategorySelected: (BoardCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 업종 카테고리
            item {
                CategorySection(
                    title = "업종",
                    categories = categories.filter { it.group == "업종" },
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        onCategorySelected(it)
                        onDismiss()
                    }
                )
            }

            // 소통 카테고리
            item {
                CategorySection(
                    title = "소통",
                    categories = categories.filter { it.group == "소통" },
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        onCategorySelected(it)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    title: String,
    categories: List<BoardCategory>,
    selectedCategory: BoardCategory?,
    onCategorySelected: (BoardCategory?) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                CustomFilterChip(
                    text = category.title,
                    isSelected = selectedCategory?.title == category.title,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}