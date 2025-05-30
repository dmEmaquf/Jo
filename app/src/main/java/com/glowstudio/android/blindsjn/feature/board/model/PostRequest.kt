package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class PostRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("industry") val category: String,
    @SerializedName("industry_id") val industryId: Int?,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("experience") val experience: String = "신입",
    @SerializedName("tags") val tags: List<String> = emptyList()
)
