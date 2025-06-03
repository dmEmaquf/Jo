// SignupRequest.kt
package com.glowstudio.android.blindsjn.data.model

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("password") val password: String,
    @SerializedName("verificationCode") val verificationCode: String
) {
    constructor(phoneNumber: String, password: String) : this(phoneNumber, password, "")
}
