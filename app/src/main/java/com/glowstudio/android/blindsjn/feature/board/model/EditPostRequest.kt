package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class EditPostRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)
