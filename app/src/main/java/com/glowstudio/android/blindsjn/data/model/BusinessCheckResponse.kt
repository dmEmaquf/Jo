package com.glowstudio.android.blindsjn.data.model

data class BusinessCheckResponse(
    val status: String,
    val message: String,
    val data: BusinessCheckData?
)

data class BusinessCheckData(
    val business_number: String,
    val company_name: String,
    val business_type: String,
    val business_status: String
) 