package com.glowstudio.android.blindsjn.feature.foodcost

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton

@Composable
fun RegisterRecipeScreen() {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var recipeItems by remember { mutableStateOf(listOf(RecipeItem())) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("레시피 제목") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        recipeItems.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = {
                        recipeItems = recipeItems.toMutableList().apply {
                            this[index] = item.copy(name = it)
                        }
                    },
                    label = { Text("재료 이름") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = item.grams,
                    onValueChange = {
                        recipeItems = recipeItems.toMutableList().apply {
                            this[index] = item.copy(grams = it)
                        }
                    },
                    label = { Text("g 수") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    recipeItems = recipeItems + RecipeItem()
                }) {
                    Text("+")
                }

                if (recipeItems.size > 1) {
                    IconButton(onClick = {
                        recipeItems = recipeItems.toMutableList().apply {
                            removeAt(index)
                        }
                    }) {
                        Text("-")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("레시피 가격") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CommonButton(
            text = "등록",
            onClick = {
                // TODO: 레시피 저장 로직 구현
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 데이터 클래스 정의

data class RecipeItem(
    val name: String = "",
    val grams: String = ""
)
