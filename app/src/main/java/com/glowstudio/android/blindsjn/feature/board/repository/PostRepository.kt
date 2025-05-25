package com.glowstudio.android.blindsjn.feature.board.repository

import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.BasicResponse
import com.glowstudio.android.blindsjn.feature.board.model.*
import retrofit2.Response
import com.glowstudio.android.blindsjn.feature.board.model.PostRequest
import com.glowstudio.android.blindsjn.feature.board.model.EditPostRequest
import com.glowstudio.android.blindsjn.feature.board.model.DeleteRequest
import com.glowstudio.android.blindsjn.feature.board.model.CommentRequest
import com.glowstudio.android.blindsjn.feature.board.model.EditCommentRequest
import com.glowstudio.android.blindsjn.feature.board.model.DeleteCommentRequest
import com.glowstudio.android.blindsjn.data.network.Network
import com.glowstudio.android.blindsjn.data.model.LikeResponse

object PostRepository {

    suspend fun savePost(request: PostRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.savePost(request)
    }

    suspend fun loadPosts(): Response<ApiResponse<List<Post>>> {
        return Network.apiService.getAllPosts()
    }

    suspend fun loadPostById(postId: Int): Response<ApiResponse<Post>> {
        return Network.apiService.getPostById(postId)
    }

    suspend fun editPost(request: EditPostRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.editPost(request)
    }

    suspend fun deletePost(request: DeleteRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.deletePost(request)
    }

    suspend fun loadComments(postId: Int): Response<ApiResponse<List<Comment>>> {
        return Network.apiService.getComments(postId)
    }

    suspend fun saveComment(request: CommentRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.saveComment(request)
    }

    suspend fun editComment(request: EditCommentRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.editComment(request)
    }

    suspend fun deleteComment(request: DeleteCommentRequest): Response<ApiResponse<BasicResponse>> {
        return Network.apiService.deleteComment(request)
    }

    suspend fun likePost(request: LikePostRequest): Response<LikeResponse> {
        return Network.apiService.toggleLike(request)
    }

    suspend fun reportPost(request: ReportRequest): Response<ApiResponse<ReportResponse>> {
        return Network.apiService.reportPost(request)
    }
}
