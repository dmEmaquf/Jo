package com.glowstudio.android.blindsjn.feature.paymanagement.repository

import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.api.PayManagementApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayManagementRepository @Inject constructor(
    private val api: PayManagementApi
) {
    suspend fun getSalesSummary(period: String, date: String): SalesSummaryResponse {
        return api.getSalesSummary(period, date)
    }

    suspend fun getSalesComparison(date: String): SalesComparisonResponse {
        return api.getSalesComparison(date)
    }
} 