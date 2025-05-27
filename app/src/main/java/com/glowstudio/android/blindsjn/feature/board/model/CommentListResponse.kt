package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class CommentListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<Comment>
)
