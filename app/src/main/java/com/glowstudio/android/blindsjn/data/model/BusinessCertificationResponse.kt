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