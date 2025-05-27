package com.glowstudio.android.blindsjn.data.network

data class BusinessCheckResponse(
    val status: String, // 사업자 상태 (01 = 유효, 02 = 말소, 03 = 사용안함 등)
    val data: List<Any>? // 추가 데이터 (필요에 따라 활용)
)