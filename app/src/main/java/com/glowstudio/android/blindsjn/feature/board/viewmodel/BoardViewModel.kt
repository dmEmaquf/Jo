package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BoardViewModel : ViewModel() {
    private val _boardCategories = MutableStateFlow<List<BoardCategory>>(emptyList())
    val boardCategories: StateFlow<List<BoardCategory>> = _boardCategories

    init {
        loadBoardCategories()
    }

    private fun loadBoardCategories() {
        _boardCategories.value = listOf(
            // 업종 그룹
            BoardCategory("🍴", "음식점 및 카페", "restaurant_cafe", "업종"),
            BoardCategory("🛍️", "쇼핑 및 리테일", "shopping_retail", "업종"),
            BoardCategory("💊", "건강 및 의료", "health_medical", "업종"),
            BoardCategory("🏨", "숙박 및 여행", "accommodation_travel", "업종"),
            BoardCategory("📚", "교육 및 학습", "education_learning", "업종"),
            BoardCategory("🎮", "여가 및 오락", "leisure_entertainment", "업종"),
            BoardCategory("💰", "금융 및 공공기관", "finance_public", "업종"),
            // 소통 그룹
            BoardCategory("🔥", "인기글", "popular", "소통"),
            BoardCategory("💬", "자유글", "free", "소통")
        )
    }
}