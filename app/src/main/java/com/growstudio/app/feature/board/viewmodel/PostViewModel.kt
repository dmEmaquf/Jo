package com.growstudio.app.feature.board.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growstudio.app.data.UserManager
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.board.repository.PostRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun loadPosts(context: Context) {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadPosts()
                if (response.isSuccessful) {
                    _posts.value = response.body()?.data ?: emptyList()
                } else {
                    _statusMessage.value = "게시글 불러오기 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun savePost(context: Context, title: String, content: String, industry: String) {
        viewModelScope.launch {
            try {
                // 현재 로그인한 사용자의 ID 가져오기
                val userId = UserManager.getUserId(context).first() ?: return@launch
                
                val request = PostRequest(title, content, userId, industry)
                val response = PostRepository.savePost(request)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message
                    loadPosts(context)
                } else {
                    _statusMessage.value = "게시글 저장 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun toggleLike(context: Context, postId: Int, onResult: (Boolean, Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                // 현재 로그인한 사용자의 ID 가져오기
                val userId = UserManager.getUserId(context).first() ?: return@launch
                
                val request = LikePostRequest(post_id = postId, user_id = userId)
                val response = PostRepository.likePost(request)
                if (response.isSuccessful) {
                    val updatedPost = PostRepository.loadPostById(postId).body()?.data
                    loadPostById(postId)
                    loadPosts(context)
                    onResult(true, updatedPost?.isLiked ?: false, updatedPost?.likeCount ?: 0)
                } else {
                    onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
                }
            } catch (e: Exception) {
                onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
            }
        }
    }

    fun loadPostById(postId: Int) {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadPostById(postId)
                if (response.isSuccessful) {
                    _selectedPost.value = response.body()?.data
                } else {
                    _statusMessage.value = "게시글 불러오기 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }
} 