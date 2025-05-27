package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class CommentRequest(
    @SerializedName("post_id") val postId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("content") val content: String
)
