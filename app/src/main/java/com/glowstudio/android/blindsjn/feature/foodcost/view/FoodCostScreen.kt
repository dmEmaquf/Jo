package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.runtime.Composable

@Composable
fun FoodCostScreen(
    onRecipeListClick: () -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {},
    onIngredientListClick: () -> Unit = {},
    onRegisterIngredientClick: () -> Unit = {},
    onNavigateToPayManagement: () -> Unit = {},
    onNavigateToFoodCost: () -> Unit = {},
) {
    MarginListScreen(
        onRecipeListClick = onRecipeListClick,
        onRegisterRecipeClick = onRegisterRecipeClick,
        onIngredientListClick = onIngredientListClick,
        onRegisterIngredientClick = onRegisterIngredientClick,
        onNavigateToPayManagement = onNavigateToPayManagement,
        onNavigateToMargin = onNavigateToFoodCost
    )
}
