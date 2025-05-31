package com.glowstudio.android.blindsjn.feature.certification

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import com.glowstudio.android.blindsjn.data.model.BusinessCertificationRequest
import kotlinx.coroutines.launch

class BusinessCertViewModel : ViewModel() {
    private val repository = BusinessCertRepository()
    val resultMessage = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    fun onBusinessCertClick(name: String, phoneNumber: String, businessNumber: String, industryId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            resultMessage.value = ""

            try {
                // 1. 이미 인증된 번호인지 확인
                val (isAlreadyCertified, message) = repository.checkAlreadyCertified(phoneNumber, businessNumber)
                if (isAlreadyCertified) {
                    resultMessage.value = message
                    return@launch
                }

                // 2. 사업자 등록번호 진위확인 API 호출
                val businessCheck = repository.checkBusinessNumberValidity(businessNumber)
                if (businessCheck == null) {
                    resultMessage.value = "사업자 정보 조회에 실패했습니다."
                    return@launch
                }

                // 3. 사업자 상태 확인
                if (!businessCheck.isCertified) {
                    resultMessage.value = "유효하지 않은 사업자번호입니다."
                    return@launch
                }

                // 4. 사업자 인증 정보 저장
                val request = BusinessCertificationRequest(
                    phoneNumber = phoneNumber,
                    businessNumber = businessNumber,
                    industryId = industryId
                )
                val response = repository.saveBusinessCertification(request)
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    resultMessage.value = "인증이 완료되었습니다."
                } else {
                    resultMessage.value = response.body()?.message ?: "인증에 실패했습니다."
                }
            } catch (e: Exception) {
                resultMessage.value = "인증 중 오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
