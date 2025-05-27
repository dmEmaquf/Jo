package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class EditCommentRequest(
    @SerializedName("comment_id") val commentId: Int,
    @SerializedName("content") val content: String
)
