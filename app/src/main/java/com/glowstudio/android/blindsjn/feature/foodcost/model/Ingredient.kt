package com.glowstudio.android.blindsjn.feature.foodcost.model

data class Ingredient(
    val ingredient_id: Int? = null,
    val name: String,
    val grams: Double,
    val price: Int
)

data class IngredientRequest(
    val name: String,
    val grams: Double,
    val price: Int
)

data class IngredientListResponse(
    val status: String,
    val data: List<Ingredient>
) 