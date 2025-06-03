package com.glowstudio.android.blindsjn.data.model

data class PhoneVerificationRequest(
    val phoneNumber: String
)

data class PhoneVerificationResponse(
    val status: String,
    val message: String
)

data class VerificationCodeRequest(
    val phoneNumber: String,
    val verificationCode: String
)

data class VerificationCodeResponse(
    val status: String,
    val message: String
) 