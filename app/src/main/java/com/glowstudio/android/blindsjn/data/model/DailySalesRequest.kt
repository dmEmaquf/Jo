package com.glowstudio.android.blindsjn.data.model

data class DailySalesRequest(
    val date: String,
    val store_sales_amount: Double,
    val delivery_sales_amount: Double
) 