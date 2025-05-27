package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class DeleteCommentRequest(
    @SerializedName("comment_id") val commentId: Int
)
