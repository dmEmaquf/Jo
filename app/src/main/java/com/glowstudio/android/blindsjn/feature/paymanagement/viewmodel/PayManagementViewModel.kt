package com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.repository.PayManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "PayManagementViewModel"

@HiltViewModel
class PayManagementViewModel @Inject constructor(
    private val repository: PayManagementRepository
) : ViewModel() {
    private val _selectedPeriod = MutableStateFlow("일")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _salesSummary = MutableStateFlow<SalesSummaryResponse?>(null)
    val salesSummary: StateFlow<SalesSummaryResponse?> = _salesSummary

    private val _salesComparison = MutableStateFlow<SalesComparisonResponse?>(null)
    val salesComparison: StateFlow<SalesComparisonResponse?> = _salesComparison

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadData()
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "데이터 로드 시작. Period: ${_selectedPeriod.value}")
            try {
                _isLoading.value = true
                _error.value = null // 새로운 로드 시작 시 에러 초기화

                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                // API에 보낼 기간 값 변환 (한국어 -> 영어)
                val periodForApi = when (_selectedPeriod.value) {
                    "일" -> "day"
                    "주" -> "week"
                    "월" -> "month"
                    "연" -> "year"
                    else -> "day" // 기본값 또는 오류 처리
                }

                // 매출 요약 데이터 로드
                Log.d(TAG, "매출 요약 API 호출 시도: period=$periodForApi, date=$today")
                val summaryResponse = repository.getSalesSummary(periodForApi, today)
                Log.d(TAG, "매출 요약 API 응답 받음: $summaryResponse")

                if (summaryResponse.status == "success") {
                    _salesSummary.value = summaryResponse
                    Log.d(TAG, "_salesSummary 상태 업데이트 완료")
                } else {
                    _error.value = summaryResponse.message ?: "매출 요약 데이터를 불러오는데 실패했습니다."
                    Log.e(TAG, "매출 요약 API 오류 응답: ${summaryResponse.message}")
                    _salesSummary.value = null // 오류 발생 시 데이터 초기화
                }

                // 매출 비교 데이터 로드
                Log.d(TAG, "매출 비교 API 호출 시도: date=$today")
                val comparisonResponse = repository.getSalesComparison(today)
                Log.d(TAG, "매출 비교 API 응답 받음: $comparisonResponse")

                 if (comparisonResponse.status == "success") {
                    _salesComparison.value = comparisonResponse
                    Log.d(TAG, "_salesComparison 상태 업데이트 완료")
                } else {
                    // 매출 비교 API 오류는 심각하지 않으면 에러 메시지를 덮어쓰지 않을 수 있음
                    // _error.value = comparisonResponse.message ?: "매출 비교 데이터를 불러오는데 실패했습니다."
                    Log.e(TAG, "매출 비교 API 오류 응답: ${comparisonResponse.message}")
                    _salesComparison.value = null // 오류 발생 시 데이터 초기화
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "데이터를 불러오는데 실패했습니다."
                Log.e(TAG, "데이터 로드 중 예외 발생", e)
                _salesSummary.value = null
                _salesComparison.value = null
            } finally {
                _isLoading.value = false
                Log.d(TAG, "데이터 로드 종료. isLoading: ${_isLoading.value}")
            }
        }
    }
} 