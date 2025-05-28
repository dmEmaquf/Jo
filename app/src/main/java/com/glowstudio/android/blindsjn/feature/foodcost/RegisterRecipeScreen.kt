package com.glowstudio.android.blindsjn.feature.foodcost

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeIngredient
import androidx.compose.runtime.collectAsState

@Composable
fun RegisterRecipeScreen() {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var recipeItems by remember { mutableStateOf(listOf(RecipeItem())) }
    val viewModel: RecipeViewModel = viewModel()
    val registerResult by viewModel.registerResult.collectAsState()

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

        if (registerResult != null) {
            Text(registerResult ?: "", color = MaterialTheme.colorScheme.primary)
        }

        CommonButton(
            text = "등록",
            onClick = {
                val ingredients = recipeItems.mapNotNull {
                    val n = it.name.trim()
                    val g = it.grams.toDoubleOrNull() ?: 0.0
                    if (n.isNotEmpty() && g > 0) RecipeIngredient(n, g) else null
                }
                val recipeTitle = title.trim()
                val recipePrice = price.toLongOrNull() ?: 0L
                val businessId = 1 // TODO: 실제 앱에서는 로그인 정보에서 받아야 함
                if (recipeTitle.isNotEmpty() && recipePrice > 0 && ingredients.isNotEmpty()) {
                    viewModel.registerRecipe(recipeTitle, recipePrice, businessId, ingredients)
                }
                // 성공 시 폼 초기화 (실제 앱에서는 성공 콜백에서 처리 권장)
                // title = "" ; price = "" ; recipeItems = listOf(RecipeItem())
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
