package com.glowstudio.android.blindsjn.feature.main.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 하단바 표시 상태를 관리하는 ViewModel
 */
class BottomBarViewModel : ViewModel() {
    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible = _isBottomBarVisible.asStateFlow()

    fun showBottomBar() {
        _isBottomBarVisible.value = true
    }

    fun hideBottomBar() {
        _isBottomBarVisible.value = false
    }
} 