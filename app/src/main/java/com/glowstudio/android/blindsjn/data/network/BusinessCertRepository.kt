package com.glowstudio.android.blindsjn.data.network

import android.util.Log
import com.glowstudio.android.blindsjn.data.model.*
import retrofit2.Response

class BusinessCertRepository {
    private val apiService = Network.apiService
    private val publicApiService = PublicApiRetrofitInstance.api

    companion object {
        private const val SERVICE_KEY = "oMaLHlh2WsmH9/ejOxmPKKA8tV7MiQNcyt2HF9ca0cCQfUdUoNisHpBiYMJfzh+GzQYLNrJSaw5yKyAJWUz7cg==" // 인증키 (Decoding)
        private const val TAG = "BusinessCertRepository"
    }

    // 사업자 등록번호 진위 확인 API 호출
    suspend fun checkBusinessNumberValidity(businessNumber: String): BusinessCertificationResponse? {
        return try {
            // 공공데이터 API 호출
            val request = BusinessStatusRequest(listOf(businessNumber))
            val response = publicApiService.checkBusinessStatus(SERVICE_KEY, "JSON", request)
            
            if (response.isSuccessful) {
                Log.d(TAG, "API Response: ${response.body()}")
                val businessStatus = response.body()?.data?.firstOrNull()
                Log.d(TAG, "Business Status: $businessStatus")
                when (businessStatus?.b_stt) {
                    "계속사업자" -> {
                        Log.d(TAG, "Valid business number: 계속사업자")
                        // 공공데이터 API 검증 성공 후 서버에 저장
                        val serverRequest = BusinessNumberRequest(
                            businessNumber = businessNumber,
                            phoneNumber = "",  // 임시값
                            industryId = 0     // 임시값
                        )
                        val serverResponse = apiService.checkBusinessNumber(serverRequest)
                        if (serverResponse.isSuccessful) {
                            serverResponse.body()?.data
                        } else {
                            Log.e(TAG, "Server API call failed: ${serverResponse.code()}, Error: ${serverResponse.errorBody()?.string()}")
                            null
                        }
                    }
                    "폐업자" -> {
                        Log.e(TAG, "폐업된 사업자번호입니다.")
                        null
                    }
                    else -> {
                        Log.e(TAG, "유효하지 않은 사업자번호입니다. Status: ${businessStatus?.b_stt}")
                        null
                    }
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()}, Error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking business number", e)
            null
        }
    }

    // 사업자 인증 정보 저장
    suspend fun saveBusinessCertification(request: BusinessCertificationRequest): Response<ApiResponse<BusinessCertificationResponse>> {
        // 1. 사업자 등록번호 유효성 검증
        val isValid = checkBusinessNumberValidity(request.businessNumber)
        if (isValid == null) {
            throw Exception("유효하지 않은 사업자 등록번호입니다.")
        }

        // 2. 사업자 인증 정보 저장
        return apiService.saveBusinessCertification(request)
    }

    // 이미 인증된 번호인지 확인 (기존 방식)
    suspend fun checkAlreadyCertified(phoneNumber: String): Boolean {
        return try {
            // 임시 요청으로 중복 체크
            val request = BusinessNumberRequest(
                businessNumber = "temp",
                phoneNumber = phoneNumber,
                industryId = 0
            )
            val response = apiService.checkBusinessNumber(request)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    body?.status == "error" && body.message?.contains("이미 등록된") == true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking certification", e)
            false
        }
    }

    // 이미 인증된 번호인지 확인 (새로운 방식)
    suspend fun checkAlreadyCertified(phoneNumber: String, businessNumber: String): Pair<Boolean, String> {
        return try {
            // 임시 요청으로 중복 체크
            val request = BusinessNumberRequest(
                businessNumber = businessNumber,
                phoneNumber = "",  // 전화번호는 중복 체크하지 않음
                industryId = 0
            )
            val response = apiService.checkBusinessNumber(request)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    when {
                        body?.status == "error" && body.message?.contains("이미 등록된 사업자번호") == true ->
                            Pair(true, "이미 인증된 사업자번호입니다.")
                        else -> Pair(false, "")
                    }
                }
                else -> Pair(false, "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking certification", e)
            Pair(false, "인증 확인 중 오류가 발생했습니다.")
        }
    }

    // 사용자의 사업자 인증 정보 조회
    suspend fun getBusinessCertification(phoneNumber: String): Response<ApiResponse<BusinessCertificationResponse>> {
        return try {
            apiService.getBusinessCertification(phoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting business certification", e)
            throw e
        }
    }

    suspend fun getIndustries(): Response<ApiResponse<List<Industry>>> {
        return try {
            apiService.getIndustries()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting industries", e)
            throw e
        }
    }
}