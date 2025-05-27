package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("experience") val experience: String = "",
    @SerializedName("time") val time: String,  // ← 이게 있어야 합니다
    @SerializedName("commentCount") val commentCount: Int,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("isLiked") val isLiked: Boolean = false
)