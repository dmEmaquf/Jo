package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class BusinessCertificationRequest(
    val phoneNumber: String,
    val businessNumber: String,
    val industryId: Int
)

data class BusinessCertificationResponse(
    @SerializedName("is_certified")
    val isCertified: Boolean,
    @SerializedName("industry_id")
    val industryId: Int,
    val businessNumber: String
)

data class Industry(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

object IndustryData {
    val industries = mapOf(
        1 to "음식점 및 카페",
        2 to "쇼핑 및 리테일",
        3 to "건강 및 의료",
        4 to "숙박 및 여행",
        5 to "교육 및 학습",
        6 to "여가 및 오락",
        7 to "금융 및 공공기관",
        8 to "일반 요식업"
    )

    fun getIndustryName(id: Int?): String {
        return id?.let { industries[it] } ?: "자유게시판"
    }
} 