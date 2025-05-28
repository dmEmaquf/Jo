package com.glowstudio.android.blindsjn.feature.foodcost.model

data class MarginSummaryResponse(
    val status: String,
    val data: MarginData
)

data class MarginData(
    val summary: MarginSummary,
    val recent_sales: List<RecentSale>
)

data class MarginSummary(
    val total_recipes: Int,
    val total_sales: Int,
    val total_cost: Int,
    val total_margin: Int,
    val avg_margin_percentage: Double
)

data class RecentSale(
    val recipe_id: Int,
    val title: String,
    val price: Int,
    val total_ingredient_price: Int,
    val margin: Double,
    val sale_date: String
) 