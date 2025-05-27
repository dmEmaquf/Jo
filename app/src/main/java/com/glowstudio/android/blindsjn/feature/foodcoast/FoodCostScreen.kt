package com.glowstudio.android.blindsjn.feature.foodcoast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Recipe(
    val name: String,
    val price: Int,
    val ingredientCost: Int,
    val margin: Int
)

@Composable
fun FoodCostScreen(
    onRegisterRecipeClick: () -> Unit,
    onRegisterIngredientClick: () -> Unit
) {
    var recipes by remember { mutableStateOf(listOf(
        Recipe("김치찌개", 8000, 3000, 5000),
        Recipe("된장국", 7000, 2500, 4500),
        Recipe("비빔밥", 9000, 3500, 5500),
        Recipe("순대국", 8500, 3200, 5300),
        Recipe("갈비탕", 12000, 5000, 7000),
        Recipe("삼겹살", 15000, 7000, 8000),
        Recipe("닭갈비", 13000, 5500, 7500),
        Recipe("부대찌개", 11000, 4500, 6500),
        Recipe("떡볶이", 6000, 2000, 4000),
        Recipe("라면", 5000, 1500, 3500),
        Recipe("김밥", 4500, 1200, 3300),
        Recipe("튀김", 7000, 2500, 4500)
    )) }
    
    var sortField by remember { mutableStateOf("name") }
    var isAscending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Recipe List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SortableHeader("레시피 이름", "name", sortField, isAscending) { field, ascending ->
                sortField = field
                isAscending = ascending
                recipes = sortRecipes(recipes, field, ascending)
            }
            SortableHeader("레시피 가격", "price", sortField, isAscending) { field, ascending ->
                sortField = field
                isAscending = ascending
                recipes = sortRecipes(recipes, field, ascending)
            }
            SortableHeader("재료 가격", "ingredientCost", sortField, isAscending) { field, ascending ->
                sortField = field
                isAscending = ascending
                recipes = sortRecipes(recipes, field, ascending)
            }
            SortableHeader("마진", "margin", sortField, isAscending) { field, ascending ->
                sortField = field
                isAscending = ascending
                recipes = sortRecipes(recipes, field, ascending)
            }
        }

        // Recipe List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(recipes) { recipe ->
                RecipeItem(recipe)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Registration Buttons
        Button(
            onClick = onRegisterRecipeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("레시피 등록")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegisterIngredientClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("재료 등록")
        }
    }
}

@Composable
fun SortableHeader(
    title: String,
    field: String,
    currentSortField: String,
    isAscending: Boolean,
    onSort: (String, Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onSort(field, if (currentSortField == field) !isAscending else false)
        }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = if (currentSortField == field) FontWeight.Bold else FontWeight.Normal
        )
        if (currentSortField == field) {
            Icon(
                imageVector = if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isAscending) "오름차순" else "내림차순",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = recipe.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
        Text(
            text = "${recipe.price}원",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
        Text(
            text = "${recipe.ingredientCost}원",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
        Text(
            text = "${recipe.margin}원",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
    }
}

private fun sortRecipes(recipes: List<Recipe>, field: String, ascending: Boolean): List<Recipe> {
    return when (field) {
        "name" -> recipes.sortedBy { it.name }.let { if (ascending) it else it.reversed() }
        "price" -> recipes.sortedBy { it.price }.let { if (ascending) it else it.reversed() }
        "ingredientCost" -> recipes.sortedBy { it.ingredientCost }.let { if (ascending) it else it.reversed() }
        "margin" -> recipes.sortedBy { it.margin }.let { if (ascending) it else it.reversed() }
        else -> recipes
    }
}
