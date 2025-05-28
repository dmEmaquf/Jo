package com.glowstudio.android.blindsjn.feature.paymanagement.model

import com.google.gson.annotations.SerializedName

data class SalesComparison(
    @SerializedName("current_sales") val currentSales: Double,
    @SerializedName("previous_sales") val previousSales: Double,
    @SerializedName("difference_rate") val differenceRate: Double,
    @SerializedName("is_increase") val isIncrease: Boolean
)

data class SalesComparisonResponse(
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String?,
    @SerializedName("comparisons") val comparisons: Map<String, SalesComparison>?,
    @SerializedName("message") val message: String?
) 