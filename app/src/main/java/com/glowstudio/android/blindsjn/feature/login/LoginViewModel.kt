package com.glowstudio.android.blindsjn.feature.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.AuthRepository
import com.glowstudio.android.blindsjn.data.network.AutoLoginManager
import com.glowstudio.android.blindsjn.data.network.isNetworkAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val autoLoginEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val showEmptyFieldsPopup: Boolean = false,
    val showInvalidCredentialsPopup: Boolean = false,
    val showNetworkErrorPopup: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var onLoginSuccess: ((Boolean) -> Unit)? = null

    fun setOnLoginSuccess(callback: (Boolean) -> Unit) {
        onLoginSuccess = callback
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber.filter { it.isDigit() }
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateAutoLogin(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoLoginEnabled = enabled)
    }

    fun checkAutoLogin(context: Context) {
        viewModelScope.launch {
            if (!isNetworkAvailable(context)) {
                _uiState.value = _uiState.value.copy(showNetworkErrorPopup = true)
                return@launch
            }

            val autoLoginEnabled = AutoLoginManager.isAutoLoginEnabled(context)
            _uiState.value = _uiState.value.copy(autoLoginEnabled = autoLoginEnabled)

            if (autoLoginEnabled) {
                AutoLoginManager.getSavedCredentials(context)?.let { (savedPhone, savedPassword) ->
                    _uiState.value = _uiState.value.copy(
                        phoneNumber = savedPhone,
                        password = savedPassword,
                        isLoading = true
                    )
                    try {
                        val success = AuthRepository.login(context, savedPhone, savedPassword)
                        if (success) {
                            onLoginSuccess?.invoke(true)
                        } else {
                            _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                    } finally {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun login(context: Context, phoneNumber: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (phoneNumber.isEmpty() || password.isEmpty()) {
                _uiState.value = _uiState.value.copy(showEmptyFieldsPopup = true)
                return@launch
            }

            if (!isNetworkAvailable(context)) {
                _uiState.value = _uiState.value.copy(showNetworkErrorPopup = true)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val success = AuthRepository.login(context, phoneNumber, password)
                if (success) {
                    AutoLoginManager.saveLoginInfo(
                        context,
                        phoneNumber,
                        password,
                        _uiState.value.autoLoginEnabled
                    )
                    onLoginSuccess?.invoke(true)
                } else {
                    _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun dismissEmptyFieldsPopup() {
        _uiState.value = _uiState.value.copy(showEmptyFieldsPopup = false)
    }

    fun dismissInvalidCredentialsPopup() {
        _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = false)
    }

    fun dismissNetworkErrorPopup() {
        _uiState.value = _uiState.value.copy(showNetworkErrorPopup = false)
    }
}