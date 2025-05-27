package com.glowstudio.android.blindsjn.feature.certification

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import kotlinx.coroutines.launch

class BusinessCertViewModel : ViewModel() {
    private val repository = BusinessCertRepository()
    val resultMessage = mutableStateOf("")

    fun onBusinessCertClick(name: String, phoneNumber: String, businessNumber: String) {
        viewModelScope.launch {
            // 1. 이미 인증된 번호인지 확인
            val isAlreadyCertified = repository.checkAlreadyCertified(phoneNumber)
            if (isAlreadyCertified) {
                resultMessage.value = "이미 인증된 번호입니다."
                return@launch
            }

            // 2. 사업자 등록번호 진위확인 API 호출
            val isValid = repository.checkBusinessNumberValidity(businessNumber)

            // 3. 결과 메시지 처리
            if (isValid) {
                resultMessage.value = "인증이 완료되었습니다."
            } else {
                resultMessage.value = "유효하지 않은 사업자 등록번호입니다."
            }
        }
    }
}
