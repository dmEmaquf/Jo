package com.glowstudio.android.blindsjn.data.api

import com.glowstudio.android.blindsjn.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.ResponseBody

interface PhoneVerificationService {
    @POST("send_verification.php")
    suspend fun sendVerificationCode(
        @Body request: PhoneVerificationRequest
    ): Response<ResponseBody>

    @POST("verify_code.php")
    suspend fun verifyCode(
        @Body request: VerificationCodeRequest
    ): Response<ResponseBody>
} 