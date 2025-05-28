package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class PostDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PostDetailData
)

data class PostDetailData(
    @SerializedName("post") val post: Post,
    @SerializedName("comments") val comments: List<Comment>
)
