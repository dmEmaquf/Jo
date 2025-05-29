package com.glowstudio.android.blindsjn.feature.board.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.board.repository.PostRepository
import com.glowstudio.android.blindsjn.data.network.Network
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _reportResult = MutableStateFlow<String?>(null)
    val reportResult: StateFlow<String?> = _reportResult

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun setStatusMessage(message: String) {
        _statusMessage.value = message
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val response = PostRepository.loadPosts()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        apiResponse.data?.let { posts ->
                            _posts.value = posts
                        }
                    }
                } else {
                    _statusMessage.value = "불러오기 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러: ${e.message}"
            }
        }
    }

    fun loadPostById(postId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("PostViewModel", "Loading post with ID: $postId")
                val response = PostRepository.loadPostById(postId)
                android.util.Log.d("PostViewModel", "Response received: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        android.util.Log.d("PostViewModel", "Response body: $apiResponse")
                        if (apiResponse.status == "success") {
                            apiResponse.data?.let { post ->
                                android.util.Log.d("PostViewModel", "Post data: $post")
                                _selectedPost.value = post.copy(
                                    title = post.title ?: "",
                                    content = post.content ?: "",
                                    category = post.category ?: "",
                                    experience = post.experience ?: "",
                                    time = post.time ?: ""
                                )
                                // 댓글 목록도 함께 로드
                                loadComments(postId)
                            } ?: run {
                                android.util.Log.e("PostViewModel", "Post data is null")
                                _error.value = "게시글 데이터가 없습니다."
                            }
                        } else {
                            android.util.Log.e("PostViewModel", "API error: ${apiResponse.message}")
                            _error.value = apiResponse.message
                        }
                    } ?: run {
                        android.util.Log.e("PostViewModel", "Response body is null")
                        _error.value = "서버 응답이 비어있습니다."
                    }
                } else {
                    android.util.Log.e("PostViewModel", "HTTP error: ${response.code()} - ${response.message()}")
                    _error.value = "서버 오류: ${response.message()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("PostViewModel", "Error loading post", e)
                _error.value = "게시글을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadComments(postId: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostViewModel", "Loading comments for post: $postId")
                val response = PostRepository.loadComments(postId)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.status == "success") {
                            android.util.Log.d("PostViewModel", "Comments loaded: ${apiResponse.data?.size ?: 0}")
                            _comments.value = apiResponse.data ?: emptyList()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PostViewModel", "Error loading comments", e)
            }
        }
    }

    fun savePost(title: String, content: String, userId: Int, industry: String, industryId: Int? = null, phoneNumber: String, experience: String = "") {
        viewModelScope.launch {
            try {
                val postRequest = PostRequest(
                    title = title,
                    content = content,
                    userId = userId,
                    category = industry,
                    industryId = industryId,
                    phoneNumber = phoneNumber,
                    experience = experience
                )
                android.util.Log.d("PostViewModel", "Saving post with request: $postRequest")
                val response = PostRepository.savePost(postRequest)
                android.util.Log.d("PostViewModel", "Save post response: ${response.body()}")
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message ?: "게시글이 저장되었습니다."
                    loadPosts()
                } else {
                    _statusMessage.value = "저장 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun editPost(postId: Int, title: String, content: String) {
        viewModelScope.launch {
            try {
                val editRequest = EditPostRequest(postId, title, content)
                val response = PostRepository.editPost(editRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message ?: "게시글이 수정되었습니다."
                    loadPostById(postId)
                } else {
                    _statusMessage.value = "수정 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                val deleteRequest = DeleteRequest(postId)
                val response = PostRepository.deletePost(deleteRequest)
                if (response.isSuccessful) {
                    _statusMessage.value = response.body()?.message ?: "게시글이 삭제되었습니다."
                    loadPosts()
                } else {
                    _statusMessage.value = "삭제 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "에러 발생: ${e.message}"
            }
        }
    }

    fun incrementLike(postId: Int) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(likeCount = post.likeCount + 1)
            } else {
                post
            }
        }
    }

    fun decrementLike(postId: Int) {
        // TODO: 서버에 좋아요 감소 요청 또는 로컬에서 처리
        // 예시: PostRepository.decrementLike(postId)
    }

    fun toggleLike(postId: Int, userId: Int, onResult: (Boolean, Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostViewModel", "Sending like request for postId: $postId, userId: $userId")
                // 서버에 좋아요 요청 전송
                val request = LikePostRequest(post_id = postId, user_id = userId)
                val response = PostRepository.likePost(request)
                
                android.util.Log.d("PostViewModel", "Received response: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        android.util.Log.d("PostViewModel", "Response body: $apiResponse")
                        if (apiResponse.status == "success") {
                            // 서버에서 받은 liked 상태로 UI 업데이트
                            val isLiked = apiResponse.liked
                            android.util.Log.d("PostViewModel", "Like status: $isLiked")
                            
                            // 현재 게시글 업데이트
                            _selectedPost.value?.let { currentPost ->
                                val newLikeCount = if (isLiked) currentPost.likeCount + 1 else currentPost.likeCount - 1
                                _selectedPost.value = currentPost.copy(
                                    isLiked = isLiked,
                                    likeCount = newLikeCount
                                )
                                android.util.Log.d("PostViewModel", "Updated post like count: $newLikeCount")
                            }
                            
                            // 게시글 목록 업데이트
                            _posts.value = _posts.value.map { post ->
                                if (post.id == postId) {
                                    val newLikeCount = if (isLiked) post.likeCount + 1 else post.likeCount - 1
                                    post.copy(
                                        isLiked = isLiked,
                                        likeCount = newLikeCount
                                    )
                                } else {
                                    post
                                }
                            }
                            
                            onResult(true, isLiked, _selectedPost.value?.likeCount ?: 0)
                        } else {
                            android.util.Log.e("PostViewModel", "Server returned error status: ${apiResponse.status}")
                            onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
                        }
                    } ?: run {
                        android.util.Log.e("PostViewModel", "Response body is null")
                        onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
                    }
                } else {
                    android.util.Log.e("PostViewModel", "Request failed with code: ${response.code()}")
                    onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
                }
            } catch (e: Exception) {
                android.util.Log.e("PostViewModel", "Error during like request", e)
                onResult(false, _selectedPost.value?.isLiked ?: false, _selectedPost.value?.likeCount ?: 0)
            }
        }
    }

    fun reportPost(postId: Int, userId: Int, reason: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostViewModel", "Attempting to report post - postId: $postId, userId: $userId, reason: $reason")
                val request = ReportRequest(postId, userId, reason)
                val response = PostRepository.reportPost(request)
                android.util.Log.d("PostViewModel", "Report response received - success: ${response.isSuccessful}, code: ${response.code()}")
                
                if (response.isSuccessful) {
                    response.body()?.let { reportResponse ->
                        android.util.Log.d("PostViewModel", "Report response body: $reportResponse")
                        if (reportResponse.success) {
                            _reportResult.value = reportResponse.message ?: "신고가 접수되었습니다."
                        } else {
                            _reportResult.value = reportResponse.error ?: "신고 처리 중 오류가 발생했습니다."
                        }
                    } ?: run {
                        android.util.Log.e("PostViewModel", "Report response body is null")
                        _reportResult.value = "서버 응답이 비어있습니다."
                    }
                } else {
                    android.util.Log.e("PostViewModel", "Report request failed with code: ${response.code()}")
                    _reportResult.value = "서버 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("PostViewModel", "Error during report request", e)
                _reportResult.value = "신고 중 오류 발생: ${e.message}"
            }
        }
    }

    fun clearReportResult() {
        _reportResult.value = null
    }
} 