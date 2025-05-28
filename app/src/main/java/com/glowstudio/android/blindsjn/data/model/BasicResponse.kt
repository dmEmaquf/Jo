package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null
)
