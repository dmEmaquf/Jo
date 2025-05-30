package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import com.glowstudio.android.blindsjn.feature.board.model.BoardCategory
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

    init {
        // 초기 카테고리 설정
        _categories.value = boardViewModel.boardCategories.value.filter { it.title != "인기 게시판" }
    }

    private val _selectedCategory = MutableStateFlow<BoardCategory?>(null)
    val selectedCategory: StateFlow<BoardCategory?> = _selectedCategory.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    fun selectCategory(category: BoardCategory) {
        _selectedCategory.value = category
    }

    fun setSelectedTags(tags: List<String>) {
        _selectedTags.value = tags
    }
} 