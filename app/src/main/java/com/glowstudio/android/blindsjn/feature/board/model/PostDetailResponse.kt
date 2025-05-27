package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class PostDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: Post?
)
