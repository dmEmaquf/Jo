package com.growstudio.app.feature.board.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growstudio.app.data.UserManager
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.board.repository.PostRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadComments(postId)
                if (response.isSuccessful) {
                    _comments.value = response.body()?.data ?: emptyList()
                } else {
                    _statusMessage.value = "댓글 불러오기 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "댓글 에러: ${e.message}"
            }
        }
    }

    fun saveComment(context: Context, postId: Int, content: String) {
        viewModelScope.launch {
            try {
                // 현재 로그인한 사용자의 ID 가져오기
                val userId = UserManager.getUserId(context).first() ?: return@launch
                
                val commentRequest = CommentRequest(postId, userId, content)
                val response = PostRepository.saveComment(commentRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    loadComments(postId)
                } else {
                    _statusMessage.value = "댓글 저장 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "댓글 저장 오류: ${e.message}"
            }
        }
    }

    // ... 나머지 메서드들 ...
} 