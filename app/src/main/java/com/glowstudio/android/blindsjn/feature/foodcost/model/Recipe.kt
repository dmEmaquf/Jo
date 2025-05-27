package com.glowstudio.android.blindsjn.feature.foodcost.model

data class Recipe(
    val recipe_id: Int,
    val title: String,
    val price: Int,
    val margin_info: MarginInfo
)

data class MarginInfo(
    val total_ingredient_price: Double,
    val margin: Double,
    val margin_percentage: Double
)

data class RecipeIngredient(
    val name: String,
    val grams: Double
)

data class RecipeRequest(
    val title: String,
    val price: Long,
    val business_id: Int,
    val ingredients: List<RecipeIngredient>
)

data class RecipeListResponse(
    val status: String,
    val data: List<Recipe>
) 