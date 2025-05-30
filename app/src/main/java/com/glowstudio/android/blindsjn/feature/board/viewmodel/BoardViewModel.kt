package com.glowstudio.android.blindsjn.feature.board.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import com.glowstudio.android.blindsjn.data.network.UserManager
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BoardViewModel : ViewModel() {
    private val repository = BusinessCertRepository()
    
    private val _boardCategories = MutableStateFlow<List<BoardCategory>>(emptyList())
    val boardCategories: StateFlow<List<BoardCategory>> = _boardCategories

    private val _isCertified = MutableStateFlow(false)
    val isCertified: StateFlow<Boolean> = _isCertified

    private val _certifiedIndustry = MutableStateFlow<String?>(null)
    val certifiedIndustry: StateFlow<String?> = _certifiedIndustry

    init {
        loadBoardCategories()
    }

    fun checkCertification(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val phoneNumber = UserManager.getPhoneNumber(context)
                Log.d("BoardViewModel", "Phone number: $phoneNumber")
                if (phoneNumber != null) {
                    _isCertified.value = repository.checkAlreadyCertified(phoneNumber)
                    Log.d("BoardViewModel", "Is certified: ${_isCertified.value}")
                    if (_isCertified.value) {
                        val certification = repository.getBusinessCertification(phoneNumber)
                        if (certification.isSuccessful) {
                            certification.body()?.data?.let { cert ->
                                val response = repository.getIndustries()
                                if (response.isSuccessful) {
                                    response.body()?.data?.let { industries ->
                                        val industry = industries.find { it.id == cert.industryId }
                                        _certifiedIndustry.value = industry?.name
                                        Log.d("BoardViewModel", "Certified industry: ${_certifiedIndustry.value}")
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BoardViewModel", "Error checking certification", e)
            }
        }
    }

    private fun loadBoardCategories() {
        _boardCategories.value = listOf(
            // 소통 카테고리
            BoardCategory("💬", "자유게시판", "free", "소통"),
            // 업종별 게시판
            BoardCategory("🍴", "음식점 및 카페", "restaurant_cafe", "업종"),
            BoardCategory("🛍️", "쇼핑 및 리테일", "shopping_retail", "업종"),
            BoardCategory("💊", "건강 및 의료", "health_medical", "업종"),
            BoardCategory("🏨", "숙박 및 여행", "accommodation_travel", "업종"),
            BoardCategory("📚", "교육 및 학습", "education_learning", "업종"),
            BoardCategory("🎮", "여가 및 오락", "leisure_entertainment", "업종"),
            BoardCategory("💰", "금융 및 공공기관", "finance_public", "업종")
        )
    }

    fun isCategoryEnabled(category: BoardCategory): Boolean {
        val enabled = when {
            category.group == "소통" -> true // 소통 카테고리(자유게시판, 인기 게시판)는 항상 활성화
            !_isCertified.value -> false // 인증되지 않은 사용자는 업종별 게시판 선택 불가
            category.title == _certifiedIndustry.value -> true // 인증된 사용자는 자신의 업종만 선택 가능
            else -> false // 그 외의 경우 선택 불가
        }
        Log.d("BoardViewModel", "Category ${category.title} enabled: $enabled")
        return enabled
    }
}