package com.glowstudio.android.blindsjn.feature.foodcoast

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton

@Composable
fun RegisterIngredientScreen() {
    var ingredientItems by remember { mutableStateOf(listOf(IngredientItem())) }

    Column(Modifier.padding(16.dp)) {
        ingredientItems.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = {
                        ingredientItems = ingredientItems.toMutableList().apply {
                            this[index] = item.copy(name = it)
                        }
                    },
                    label = { Text("재료 이름") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(4.dp))

                OutlinedTextField(
                    value = item.grams,
                    onValueChange = {
                        ingredientItems = ingredientItems.toMutableList().apply {
                            this[index] = item.copy(grams = it)
                        }
                    },
                    label = { Text("g 수") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(4.dp))

                OutlinedTextField(
                    value = item.price,
                    onValueChange = {
                        ingredientItems = ingredientItems.toMutableList().apply {
                            this[index] = item.copy(price = it)
                        }
                    },
                    label = { Text("가격") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    ingredientItems = ingredientItems + IngredientItem()
                }) {
                    Text("+")
                }

                if (ingredientItems.size > 1) {
                    IconButton(onClick = {
                        ingredientItems = ingredientItems.toMutableList().apply {
                            removeAt(index)
                        }
                    }) {
                        Text("-")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CommonButton(
            text = "등록",
            onClick = {
                // TODO: 저장 로직 구현 후 이전 화면으로 이동
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 데이터 클래스 정의

data class IngredientItem(
    val name: String = "",
    val grams: String = "",
    val price: String = ""
)
