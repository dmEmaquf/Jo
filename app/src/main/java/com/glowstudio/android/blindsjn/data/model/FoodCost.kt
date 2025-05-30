package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val totalCost: Double,
    val sellingPrice: Double,
    val margin: Double,
    val userId: Int
)

data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val costPerUnit: Double,
    val totalCost: Double
)

data class CreateRecipeRequest(
    val name: String,
    val description: String,
    val ingredients: List<CreateIngredientRequest>,
    val sellingPrice: Double,
    val userId: Int
)

data class CreateIngredientRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val costPerUnit: Double
)

data class RecipeResponse(
    val status: String,
    val message: String,
    val data: Recipe?
)

data class RecipeListResponse(
    val status: String,
    val message: String,
    val data: List<Recipe>?
) 