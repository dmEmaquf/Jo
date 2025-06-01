package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class PostRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("industry_id") val industryId: Int?,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("industry") val industry: String = category
)
