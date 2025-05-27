package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.board.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

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

    fun saveComment(postId: Int, userId: Int, content: String) {
        viewModelScope.launch {
            try {
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

    fun editComment(commentId: Int, content: String) {
        viewModelScope.launch {
            try {
                val editRequest = EditCommentRequest(commentId, content)
                val response = PostRepository.editComment(editRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    // 현재 보고 있는 게시글의 댓글 목록을 다시 로드
                    _comments.value.firstOrNull()?.postId?.let { postId ->
                        loadComments(postId)
                    }
                } else {
                    _statusMessage.value = "댓글 수정 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "댓글 수정 오류: ${e.message}"
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            try {
                val deleteRequest = DeleteCommentRequest(commentId)
                val response = PostRepository.deleteComment(deleteRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    // 현재 보고 있는 게시글의 댓글 목록을 다시 로드
                    _comments.value.firstOrNull()?.postId?.let { postId ->
                        loadComments(postId)
                    }
                } else {
                    _statusMessage.value = "댓글 삭제 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "댓글 삭제 오류: ${e.message}"
            }
        }
    }
}
