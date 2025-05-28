package com.glowstudio.android.blindsjn.feature.paymanagement.model

import com.google.gson.annotations.SerializedName

data class TopItemsResponse(
    val status: String,
    val period: String,
    val date: String,
    @SerializedName("period_dates") val periodDates: Map<String, String>? = null,
    @SerializedName("top_items") val topItems: List<TopItem>? = null,
    val summary: TopItemsSummary? = null,
    val message: String? = null
)

data class TopItem(
    @SerializedName("recipe_id") val recipeId: Int,
    @SerializedName("recipe_name") val recipeName: String,
    @SerializedName("recipe_price") val recipePrice: Double,
    @SerializedName("total_sales") val totalSales: Double,
    @SerializedName("total_ingredient_price") val totalIngredientPrice: Double,
    @SerializedName("margin") val margin: Double,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("total_margin") val totalMargin: Double,
    @SerializedName("margin_rate") val marginRate: Double,
    @SerializedName("sales_ratio") val salesRatio: Double
)

data class TopItemsSummary(
    @SerializedName("total_sales") val totalSales: Double,
    @SerializedName("total_margin") val totalMargin: Double,
    @SerializedName("margin_rate") val marginRate: Double
) 