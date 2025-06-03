package com.glowstudio.android.blindsjn.feature.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.model.PhoneVerificationResponse
import com.glowstudio.android.blindsjn.data.model.VerificationCodeResponse
import com.glowstudio.android.blindsjn.data.repository.PhoneVerificationRepository
import kotlinx.coroutines.launch

class PhoneVerificationViewModel(
    private val repository: PhoneVerificationRepository
) : ViewModel() {

    private val _verificationState = MutableLiveData<VerificationState>()
    val verificationState: LiveData<VerificationState> = _verificationState

    fun sendVerificationCode(phoneNumber: String) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            repository.sendVerificationCode(phoneNumber)
                .onSuccess { response ->
                    _verificationState.value = if (response.status == "success") {
                        VerificationState.CodeSent(response.message)
                    } else {
                        VerificationState.Error(response.message)
                    }
                }
                .onFailure { error ->
                    _verificationState.value = VerificationState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun verifyCode(phoneNumber: String, code: String) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            repository.verifyCode(phoneNumber, code)
                .onSuccess { response ->
                    _verificationState.value = if (response.status == "success") {
                        VerificationState.Verified(response.message)
                    } else {
                        VerificationState.Error(response.message)
                    }
                }
                .onFailure { error ->
                    _verificationState.value = VerificationState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class VerificationState {
    object Loading : VerificationState()
    data class CodeSent(val message: String) : VerificationState()
    data class Verified(val message: String) : VerificationState()
    data class Error(val message: String) : VerificationState()
} 