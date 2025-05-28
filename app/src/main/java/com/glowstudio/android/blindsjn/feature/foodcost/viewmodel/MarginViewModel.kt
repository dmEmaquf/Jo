package com.glowstudio.android.blindsjn.feature.foodcost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.InternalServer
import com.glowstudio.android.blindsjn.feature.foodcost.model.MarginData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarginViewModel : ViewModel() {
    private val _marginData = MutableStateFlow<MarginData?>(null)
    val marginData: StateFlow<MarginData?> = _marginData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMarginData(businessId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = InternalServer.api.getMarginSummary(businessId)
                if (response.isSuccessful) {
                    response.body()?.let { marginResponse ->
                        if (marginResponse.status == "success") {
                            _marginData.value = marginResponse.data
                        } else {
                            _error.value = "마진 데이터를 불러오는데 실패했습니다."
                        }
                    } ?: run {
                        _error.value = "응답 데이터가 없습니다."
                    }
                } else {
                    _error.value = "서버 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류 발생: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 