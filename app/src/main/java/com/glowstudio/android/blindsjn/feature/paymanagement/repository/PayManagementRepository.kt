package com.glowstudio.android.blindsjn.feature.paymanagement.repository

import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.TopItemsResponse
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PayManagementRepository @Inject constructor(
    private val api: PayManagementApi,
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("pay_management_prefs", Context.MODE_PRIVATE)
    private val MONTHLY_GOAL_KEY = "monthly_sales_goal"

    suspend fun getSalesSummary(date: String): SalesSummaryResponse {
        return api.getSalesSummary(date)
    }

    suspend fun getSalesComparison(date: String): SalesComparisonResponse {
        return api.getSalesComparison(date)
    }

    suspend fun getTopItems(date: String, period: String = "day"): TopItemsResponse {
        return api.getTopItems(date, period)
    }

    fun getMonthlyGoal(): Double {
        return sharedPreferences.getFloat(MONTHLY_GOAL_KEY, 3500000f).toDouble()
    }

    fun saveMonthlyGoal(goal: Double) {
        sharedPreferences.edit().putFloat(MONTHLY_GOAL_KEY, goal.toFloat()).apply()
    }
} 