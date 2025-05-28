package com.glowstudio.android.blindsjn.feature.paymanagement.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesSummaryResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.SalesComparisonResponse
import com.glowstudio.android.blindsjn.feature.paymanagement.model.TopItemsResponse
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

    private val _topItems = MutableStateFlow<TopItemsResponse?>(null)
    val topItems: StateFlow<TopItemsResponse?> = _topItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _weeklySales = MutableStateFlow<List<Double>>(emptyList())
    val weeklySales: StateFlow<List<Double>> = _weeklySales

    private val _weeklyAverage = MutableStateFlow<Double>(0.0)
    val weeklyAverage: StateFlow<Double> = _weeklyAverage

    private val _monthlyGoal = MutableStateFlow<Double>(3500000.0) // 기본값 350만원
    val monthlyGoal: StateFlow<Double> = _monthlyGoal

    private val _monthlyProgress = MutableStateFlow<Double>(0.0)
    val monthlyProgress: StateFlow<Double> = _monthlyProgress

    private val _showGoalSettingDialog = MutableStateFlow(false)
    val showGoalSettingDialog: StateFlow<Boolean> = _showGoalSettingDialog

    private val _dailyAverage = MutableStateFlow<Double>(0.0)
    val dailyAverage: StateFlow<Double> = _dailyAverage

    private val _dailyComparison = MutableStateFlow<Double>(0.0)
    val dailyComparison: StateFlow<Double> = _dailyComparison

    init {
        loadData()
        loadWeeklySales()
        loadMonthlyGoal()
        loadDailyAverage()
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        loadData()
    }

    fun setMonthlyGoal(goal: Double) {
        repository.saveMonthlyGoal(goal)
        _monthlyGoal.value = goal
    }

    fun showGoalSettingDialog() {
        _showGoalSettingDialog.value = true
    }

    fun hideGoalSettingDialog() {
        _showGoalSettingDialog.value = false
    }

    private fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "데이터 로드 시작. Period: ${_selectedPeriod.value}")
            try {
                _isLoading.value = true
                _error.value = null

                val today = LocalDate.now()
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                
                // 이번 달의 매출 데이터를 가져옵니다
                var totalMonthlySales = 0.0
                var currentDate = startOfMonth
                
                while (!currentDate.isAfter(endOfMonth)) {
                    if (!currentDate.isAfter(today)) { // 오늘까지의 데이터만 합산
                        val dateStr = currentDate.format(DateTimeFormatter.ISO_DATE)
                        val response = repository.getSalesSummary(dateStr)
                        if (response.status == "success" && response.summary != null) {
                            totalMonthlySales += response.summary.totalSales
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                _monthlyProgress.value = totalMonthlySales
                
                // 기존 데이터 로드 로직...
                val periodForApi = when (_selectedPeriod.value) {
                    "일" -> "day"
                    "주" -> "week"
                    "월" -> "month"
                    "연" -> "year"
                    else -> "day"
                }

                // 매출 요약 데이터 로드
                val summaryResponse = repository.getSalesSummary(today.format(DateTimeFormatter.ISO_DATE))
                if (summaryResponse.status == "success") {
                    _salesSummary.value = summaryResponse
                } else {
                    _error.value = summaryResponse.message ?: "매출 요약 데이터를 불러오는데 실패했습니다."
                    _salesSummary.value = null
                }

                // 매출 비교 데이터 로드
                val comparisonResponse = repository.getSalesComparison(today.format(DateTimeFormatter.ISO_DATE))
                if (comparisonResponse.status == "success") {
                    _salesComparison.value = comparisonResponse
                } else {
                    _salesComparison.value = null
                }

                // TOP 3 메뉴 데이터 로드
                val topItemsResponse = repository.getTopItems(today.format(DateTimeFormatter.ISO_DATE), periodForApi)
                if (topItemsResponse.status == "success") {
                    _topItems.value = topItemsResponse
                } else {
                    _error.value = topItemsResponse.message ?: "TOP 3 메뉴 데이터를 불러오는데 실패했습니다."
                    _topItems.value = null
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "데이터를 불러오는데 실패했습니다."
                Log.e(TAG, "데이터 로드 중 예외 발생", e)
                _salesSummary.value = null
                _salesComparison.value = null
                _topItems.value = null
            } finally {
                _isLoading.value = false
                Log.d(TAG, "데이터 로드 종료. isLoading: ${_isLoading.value}")
            }
        }
    }

    private fun loadWeeklySales() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                val weeklyData = mutableListOf<Double>()
                
                // 이번 주의 매출 데이터를 가져옵니다
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    // 현재 요일 이후의 미래 날짜는 매출을 0으로 설정
                    if (date.isAfter(today)) {
                        weeklyData.add(0.0)
                        continue
                    }
                    
                    val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                    val response = repository.getSalesSummary(dateStr)
                    if (response.status == "success" && response.summary != null) {
                        weeklyData.add(response.summary.totalSales)
                    } else {
                        weeklyData.add(0.0)
                    }
                }
                _weeklySales.value = weeklyData
                
                // 주간 평균 매출 계산 (미래 날짜 제외)
                val validSales = weeklyData.filter { it > 0 }
                _weeklyAverage.value = if (validSales.isNotEmpty()) {
                    validSales.average()
                } else {
                    0.0
                }
            } catch (e: Exception) {
                Log.e(TAG, "주간 매출 데이터 로드 중 오류 발생", e)
                _weeklySales.value = List(7) { 0.0 }
                _weeklyAverage.value = 0.0
            }
        }
    }

    private fun loadMonthlyGoal() {
        _monthlyGoal.value = repository.getMonthlyGoal()
    }

    private fun loadDailyAverage() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val dayOfWeek = today.dayOfWeek.value
                
                // 이번 달의 같은 요일 매출 데이터를 가져옵니다
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                var totalSales = 0.0
                var count = 0
                
                var currentDate = startOfMonth
                while (!currentDate.isAfter(endOfMonth)) {
                    if (currentDate.dayOfWeek.value == dayOfWeek && !currentDate.isAfter(today)) {
                        val dateStr = currentDate.format(DateTimeFormatter.ISO_DATE)
                        val response = repository.getSalesSummary(dateStr)
                        if (response.status == "success" && response.summary != null) {
                            totalSales += response.summary.totalSales
                            count++
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                // 평균 계산
                _dailyAverage.value = if (count > 0) totalSales / count else 0.0
                
                // 다른 요일 평균과 비교
                var otherDaysTotal = 0.0
                var otherDaysCount = 0
                currentDate = startOfMonth
                while (!currentDate.isAfter(endOfMonth)) {
                    if (currentDate.dayOfWeek.value != dayOfWeek && !currentDate.isAfter(today)) {
                        val dateStr = currentDate.format(DateTimeFormatter.ISO_DATE)
                        val response = repository.getSalesSummary(dateStr)
                        if (response.status == "success" && response.summary != null) {
                            otherDaysTotal += response.summary.totalSales
                            otherDaysCount++
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                val otherDaysAverage = if (otherDaysCount > 0) otherDaysTotal / otherDaysCount else 0.0
                _dailyComparison.value = if (otherDaysAverage > 0) {
                    ((_dailyAverage.value - otherDaysAverage) / otherDaysAverage) * 100
                } else 0.0
                
            } catch (e: Exception) {
                Log.e(TAG, "요일별 평균 매출 계산 중 오류 발생", e)
                _dailyAverage.value = 0.0
                _dailyComparison.value = 0.0
            }
        }
    }

    fun refresh() {
        loadData()
        loadWeeklySales()
        loadDailyAverage()
    }
} 