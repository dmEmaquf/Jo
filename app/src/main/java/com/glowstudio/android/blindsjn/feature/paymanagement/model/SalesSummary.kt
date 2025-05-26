package com.glowstudio.android.blindsjn.feature.paymanagement.model

import com.google.gson.annotations.SerializedName

data class SalesSummary(
    @SerializedName("date") val date: String,
    @SerializedName("total_sales_amount") val totalSalesAmount: Double,
    @SerializedName("total_margin_amount") val totalMarginAmount: Double,
    @SerializedName("day_of_week") val dayOfWeek: String
)

data class SalesSummaryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("period") val period: String,
    @SerializedName("date") val date: String?,
    @SerializedName("requested_date") val requestedDate: String?,
    @SerializedName("actual_date") val actualDate: String?,
    @SerializedName("data") val data: List<SalesSummary>?,
    @SerializedName("summary") val summary: Summary?,
    @SerializedName("message") val message: String?
)

data class Summary(
    @SerializedName("total_sales") val totalSales: Double,
    @SerializedName("total_margin") val totalMargin: Double,
    @SerializedName("margin_rate") val marginRate: Double
) 