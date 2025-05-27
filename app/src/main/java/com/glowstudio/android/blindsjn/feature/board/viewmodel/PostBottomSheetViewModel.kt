package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostBottomSheetViewModel : ViewModel() {
    // 전체 태그 목록
    private val _tags = MutableStateFlow<List<String>>(listOf("누구나", "재학생", "패션디자인", "비활성1", "비활성2"))
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    // 선택 가능한 태그 목록
    private val _enabledTags = MutableStateFlow<List<String>>(listOf("누구나", "재학생", "패션디자인"))
    val enabledTags: StateFlow<List<String>> = _enabledTags.asStateFlow()

    // 선택된 태그 목록
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    fun toggleTag(tag: String) {
        if (!_enabledTags.value.contains(tag)) return
        _selectedTags.value = if (_selectedTags.value.contains(tag)) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun clearSelection() {
        _selectedTags.value = emptySet()
    }

    fun setTags(tags: List<String>, enabledTags: List<String>) {
        _tags.value = tags
        _enabledTags.value = enabledTags
        _selectedTags.value = emptySet()
    }
} 