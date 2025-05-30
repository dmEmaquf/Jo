package com.glowstudio.android.blindsjn.feature.ocr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.model.DailySalesRequest
import com.glowstudio.android.blindsjn.data.model.DailySalesResponse
import com.glowstudio.android.blindsjn.data.network.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DailySalesSaveState {
    object Idle : DailySalesSaveState()
    object Loading : DailySalesSaveState()
    data class Success(val response: DailySalesResponse) : DailySalesSaveState()
    data class Error(val message: String) : DailySalesSaveState()
}

class DailySalesViewModel : ViewModel() {
    private val _saveState = MutableStateFlow<DailySalesSaveState>(DailySalesSaveState.Idle)
    val saveState: StateFlow<DailySalesSaveState> = _saveState

    fun saveDailySales(request: DailySalesRequest) {
        _saveState.value = DailySalesSaveState.Loading
        viewModelScope.launch {
            try {
                val response = Network.apiService.saveDailySales(request)
                if (response.isSuccessful && response.body() != null) {
                    _saveState.value = DailySalesSaveState.Success(response.body()!!)
                } else {
                    _saveState.value = DailySalesSaveState.Error(response.message())
                }
            } catch (e: Exception) {
                _saveState.value = DailySalesSaveState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun resetState() {
        _saveState.value = DailySalesSaveState.Idle
    }
} 