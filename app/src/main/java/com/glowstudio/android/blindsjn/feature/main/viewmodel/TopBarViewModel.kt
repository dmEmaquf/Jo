package com.glowstudio.android.blindsjn.feature.main.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TopBarType { MAIN, DETAIL }

/**
 * 상단바 상태 데이터 클래스
 * title: 상단바에 표시할 제목
 * showBackButton: 뒤로가기 버튼 표시 여부
 * showSearchButton: 검색 버튼 표시 여부
 */
data class TopBarState(
    val type: TopBarType = TopBarType.MAIN,
    val title: String = "",
    val showBackButton: Boolean = false,
    val showSearchButton: Boolean = false,
    val showMoreButton: Boolean = false,
    val showNotificationButton: Boolean = false,
    val onBackClick: () -> Unit = {},
    val onSearchClick: () -> Unit = {},
    val onMoreClick: () -> Unit = {},
    val onNotificationClick: () -> Unit = {}
)

/**
 * 상단바 상태를 관리하는 ViewModel
 */
class TopBarViewModel : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState = _topBarState.asStateFlow()

    fun updateState(newState: TopBarState) {
        _topBarState.value = newState
    }

    fun setMainBar(
        onSearchClick: () -> Unit = {},
        onMoreClick: () -> Unit = {},
        onNotificationClick: () -> Unit = {}
    ) {
        _topBarState.value = TopBarState(
            type = TopBarType.MAIN,
            showSearchButton = true,
            showMoreButton = true,
            showNotificationButton = true,
            onSearchClick = onSearchClick,
            onMoreClick = onMoreClick,
            onNotificationClick = onNotificationClick
        )
    }

    fun setDetailBar(
        title: String,
        showBackButton: Boolean = true,
        showSearchButton: Boolean = true,
        showMoreButton: Boolean = true,
        onBackClick: () -> Unit = {},
        onSearchClick: () -> Unit = {},
        onMoreClick: () -> Unit = {}
    ) {
        _topBarState.value = TopBarState(
            type = TopBarType.DETAIL,
            title = title,
            showBackButton = showBackButton,
            showSearchButton = showSearchButton,
            showMoreButton = showMoreButton,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onMoreClick = onMoreClick
        )
    }
}
