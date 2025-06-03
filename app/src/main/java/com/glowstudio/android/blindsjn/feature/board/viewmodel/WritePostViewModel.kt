package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
import com.glowstudio.android.blindsjn.data.model.IndustryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class WritePostViewModel(
    private val boardViewModel: BoardViewModel
) : ViewModel() {
    // 인기 게시판을 제외한 카테고리만 표시
    private val _categories = MutableStateFlow<List<BoardCategory>>(emptyList())
    val categories: StateFlow<List<BoardCategory>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<BoardCategory?>(null)
    val selectedCategory: StateFlow<BoardCategory?> = _selectedCategory.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    init {
        // 초기 카테고리 설정
        _categories.value = boardViewModel.boardCategories.value.filter { it.title != "인기 게시판" }
        
        // 인증된 사용자의 경우 해당 업종의 카테고리 자동 선택
        boardViewModel.certifiedIndustry.value?.let { industry ->
            _categories.value.find { it.title == industry }?.let { category ->
                _selectedCategory.value = category
            }
        }
    }

    fun selectCategory(category: BoardCategory) {
        _selectedCategory.value = category
    }

    fun setSelectedTags(tags: List<String>) {
        _selectedTags.value = tags
    }

    // 선택된 카테고리의 업종 ID를 반환
    fun getSelectedIndustryId(): Int? {
        return _selectedCategory.value?.let { category ->
            android.util.Log.d("WritePostViewModel", "Getting industry ID for category: ${category.title}")
            if (category.title == "자유게시판") {
                android.util.Log.d("WritePostViewModel", "Category is 자유게시판, returning 9")
                9
            } else {
                // 카테고리 제목과 일치하는 업종 ID 찾기
                val industryId = IndustryData.industries.entries
                    .find { (_, name) -> name == category.title }
                    ?.key
                
                android.util.Log.d("WritePostViewModel", "Found industry ID: $industryId for category: ${category.title}")
                industryId
            }
        }
    }
} 