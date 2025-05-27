// SignupRequest.kt
package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("password") val password: String
)
