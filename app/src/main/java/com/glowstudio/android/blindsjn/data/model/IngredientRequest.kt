package com.glowstudio.android.blindsjn.data.model

data class IngredientRequest(
    val name: String,
    val price: Int,
    val business_id: Int,
    val ingredient_id: Int? = null
) 