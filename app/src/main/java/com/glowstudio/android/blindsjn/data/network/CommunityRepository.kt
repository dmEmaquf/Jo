package com.glowstudio.android.blindsjn.data.network

import com.glowstudio.android.blindsjn.data.model.*
import com.glowstudio.android.blindsjn.feature.board.model.*
import retrofit2.Response

class CommunityRepository {
    private val apiService = Network.apiService

    suspend fun getPosts(industryId: Int? = null): Response<ApiResponse<List<Post>>> {
        return if (industryId != null) {
            apiService.getPostsByIndustry(industryId)
        } else {
            apiService.getAllPosts()
        }
    }

    suspend fun createPost(request: PostRequest): Response<ApiResponse<BasicResponse>> {
        // 업종별 게시판인 경우 사업자 인증 확인
        if (request.industryId != null) {
            val response = apiService.getBusinessCertification(request.userId.toString())
            val isCertified = response.body()?.data?.isCertified ?: false
            if (!isCertified) {
                throw Exception("업종별 게시판은 사업자 인증이 필요합니다.")
            }

            // 인증된 업종과 게시글 작성하려는 업종이 일치하는지 확인
            val certification = response.body()?.data
            if (certification?.industryId != request.industryId) {
                throw Exception("인증된 업종의 게시판에만 글을 작성할 수 있습니다.")
            }
        }

        return apiService.savePost(request)
    }

    suspend fun updatePost(request: EditPostRequest): Response<ApiResponse<BasicResponse>> {
        return apiService.editPost(request)
    }

    suspend fun deletePost(request: DeleteRequest): Response<ApiResponse<BasicResponse>> {
        return apiService.deletePost(request)
    }

    suspend fun getPostById(postId: Int): Response<ApiResponse<Post>> {
        return apiService.getPostById(postId)
    }
} 