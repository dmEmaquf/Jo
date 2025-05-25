package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val user_id: Int
) 