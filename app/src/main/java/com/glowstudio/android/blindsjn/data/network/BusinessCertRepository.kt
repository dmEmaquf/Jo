package com.glowstudio.android.blindsjn.data.network

import android.util.Log
import com.glowstudio.android.blindsjn.data.model.*
import retrofit2.Response

class BusinessCertRepository {
    private val apiService = Network.apiService

    companion object {
    private const val SERVICE_KEY = "oMaLHlh2WsmH9%2FejOxmPKKA8tV7MiQNcyt2HF9ca0cCQfUdUoNisHpBiYMJfzh%2BGzQYLNrJSaw5yKyAJWUz7cg%3D%3D" // 인증키 (Encoding)
    }

    // 사업자 등록번호 진위 확인 API 호출
    suspend fun checkBusinessNumberValidity(businessNumber: String): Boolean {
        return try {
            val response = apiService.checkBusinessCertification(businessNumber)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    // 사업자 인증 정보 저장
    suspend fun saveBusinessCertification(request: BusinessCertificationRequest): Response<ApiResponse<BusinessCertificationResponse>> {
        return apiService.saveBusinessCertification(request)
    }

    // 이미 인증된 번호인지 확인
    suspend fun checkAlreadyCertified(phoneNumber: String): Boolean {
        return try {
            val response = apiService.checkBusinessCertification(phoneNumber)
            response.isSuccessful && response.body()?.data != null
        } catch (e: Exception) {
            false
        }
    }

    // 사용자의 사업자 인증 정보 조회
    suspend fun getBusinessCertification(phoneNumber: String): Response<ApiResponse<BusinessCertificationResponse>> {
        return apiService.getBusinessCertification(phoneNumber)
    }

    suspend fun getIndustries(): Response<ApiResponse<List<Industry>>> {
        return apiService.getIndustries()
    }
}