package com.glowstudio.android.blindsjn.data.model

data class RecipeRequest(
    val title: String,
    val price: Int,
    val business_id: Int,
    val ingredients: List<IngredientItem>,
    val recipe_id: Int? = null
)

data class IngredientItem(
    val name: String,
    val grams: Double
) 