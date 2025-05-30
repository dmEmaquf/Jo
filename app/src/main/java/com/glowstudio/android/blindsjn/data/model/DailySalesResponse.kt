package com.glowstudio.android.blindsjn.data.model

data class DailySalesResponse(
    val status: String,
    val message: String?,
    val data: Map<String, Any>?
) 