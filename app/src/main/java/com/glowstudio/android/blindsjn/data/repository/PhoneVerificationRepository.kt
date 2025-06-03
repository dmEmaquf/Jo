package com.glowstudio.android.blindsjn.data.repository

import com.glowstudio.android.blindsjn.data.api.PhoneVerificationService
import com.glowstudio.android.blindsjn.data.model.*
import com.glowstudio.android.blindsjn.data.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody

class PhoneVerificationRepository {
    private val service: PhoneVerificationService = Network.phoneVerificationService
    private val gson = Gson()

    suspend fun sendVerificationCode(phoneNumber: String): Result<PhoneVerificationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = service.sendVerificationCode(PhoneVerificationRequest(phoneNumber))
            Log.d("PhoneVerification", "Response code: ${response.code()}")
            Log.d("PhoneVerification", "Response headers: ${response.headers()}")
            
            val responseBody = response.body()?.string()
            Log.d("PhoneVerification", "Raw response body: $responseBody")
            Log.d("PhoneVerification", "Response body length: ${responseBody?.length}")
            Log.d("PhoneVerification", "Response body bytes: ${responseBody?.toByteArray()?.joinToString { it.toString(16) }}")

            if (response.isSuccessful && responseBody != null) {
                try {
                    // 응답이 {main}인 경우 특별 처리
                    if (responseBody.trim() == "{main}") {
                        Log.e("PhoneVerification", "Received {main} response, this indicates a server-side issue")
                        return@withContext Result.failure(Exception("서버 응답에 문제가 있습니다. 관리자에게 문의해주세요."))
                    }

                    val verificationResponse = gson.fromJson(responseBody, PhoneVerificationResponse::class.java)
                    Result.success(verificationResponse)
                } catch (e: JsonSyntaxException) {
                    Log.e("PhoneVerification", "JSON parsing error: ${e.message}")
                    Log.e("PhoneVerification", "Error location: ${e.cause?.message}")
                    Result.failure(Exception("서버 응답을 처리할 수 없습니다: ${e.message}"))
                }
            } else {
                Log.e("PhoneVerification", "Error response: $responseBody")
                Result.failure(Exception("인증번호 발송에 실패했습니다: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PhoneVerification", "Exception during verification: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun verifyCode(phoneNumber: String, code: String): Result<VerificationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = service.verifyCode(VerificationCodeRequest(phoneNumber, code))
            Log.d("PhoneVerification", "Verify response code: ${response.code()}")
            Log.d("PhoneVerification", "Verify response headers: ${response.headers()}")
            
            val responseBody = response.body()?.string()
            Log.d("PhoneVerification", "Raw verify response body: $responseBody")
            Log.d("PhoneVerification", "Verify response body length: ${responseBody?.length}")
            Log.d("PhoneVerification", "Verify response body bytes: ${responseBody?.toByteArray()?.joinToString { it.toString(16) }}")

            if (response.isSuccessful && responseBody != null) {
                try {
                    // 응답이 {main}인 경우 특별 처리
                    if (responseBody.trim() == "{main}") {
                        Log.e("PhoneVerification", "Received {main} response, this indicates a server-side issue")
                        return@withContext Result.failure(Exception("서버 응답에 문제가 있습니다. 관리자에게 문의해주세요."))
                    }

                    val verificationResponse = gson.fromJson(responseBody, VerificationCodeResponse::class.java)
                    Result.success(verificationResponse)
                } catch (e: JsonSyntaxException) {
                    Log.e("PhoneVerification", "JSON parsing error: ${e.message}")
                    Log.e("PhoneVerification", "Error location: ${e.cause?.message}")
                    Result.failure(Exception("서버 응답을 처리할 수 없습니다: ${e.message}"))
                }
            } else {
                Log.e("PhoneVerification", "Error verify response: $responseBody")
                Result.failure(Exception("인증번호 확인에 실패했습니다: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PhoneVerification", "Exception during verification: ${e.message}", e)
            Result.failure(e)
        }
    }
} 