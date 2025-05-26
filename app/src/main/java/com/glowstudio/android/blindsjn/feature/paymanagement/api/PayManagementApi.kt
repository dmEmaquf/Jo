package com.glowstudio.android.blindsjn.feature.paymanagement.api

import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PayManagementApi {
    @GET("api_sales_summary.php")
    suspend fun getSalesSummary(
        @Query("period") period: String,
        @Query("date") date: String
    ): SalesSummaryResponse

    @GET("api_sales_comparison.php")
    suspend fun getSalesComparison(
        @Query("date") date: String
    ): SalesComparisonResponse
} 