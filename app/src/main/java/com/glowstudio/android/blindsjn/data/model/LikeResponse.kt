package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class LikeResponse(
    @SerializedName("status") val status: String,
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("message") val message: String
) 