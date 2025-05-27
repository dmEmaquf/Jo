package com.glowstudio.android.blindsjn.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.NaverNewsItem
import com.glowstudio.android.blindsjn.data.network.NaverNewsServer
import kotlinx.coroutines.launch
import android.util.Log

data class NaverNewsUiState(
    val newsList: List<NaverNewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewsViewModel : ViewModel() {

    private val _uiState = mutableStateOf(NaverNewsUiState())
    val uiState: State<NaverNewsUiState> = _uiState

    fun searchNews(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d("NewsViewModel", "네이버 뉴스 요청 시작: $query")
                val response = NaverNewsServer.apiService.searchNews(query)

                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    _uiState.value = NaverNewsUiState(newsList = items)
                    Log.d("NewsViewModel", "뉴스 요청 성공 - 결과 수: ${items.size}")
                } else {
                    Log.e(
                        "NewsViewModel",
                        "뉴스 요청 실패 - 코드: ${response.code()} / 메시지: ${response.message()}"
                    )
                    _uiState.value = _uiState.value.copy(error = "응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "뉴스 요청 예외 발생", e)
                _uiState.value = _uiState.value.copy(error = "예외 발생: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}