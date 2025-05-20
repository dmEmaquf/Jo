package com.glowstudio.android.blindsjn.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.BasicResponse
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import com.glowstudio.android.blindsjn.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.*

// ✅ 네트워크 상태 확인 함수
fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// ✅ Retrofit API 인터페이스
interface ApiService {

    // 🔹 회원가입 / 로그인
    @POST("signup.php")
    suspend fun signup(@Body request: SignupRequest): Response<BasicResponse>

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    // 🔹 게시글
    @GET("Get_post_by_id.php")
    suspend fun getPostById(@Query("post_id") postId: Int): Response<PostDetailResponse>

    @POST("Save_post.php")
    suspend fun savePost(@Body request: PostRequest): Response<BasicResponse>

    @PUT("Edit_post.php")
    suspend fun editPost(@Body request: EditPostRequest): Response<BasicResponse>

    @HTTP(method = "DELETE", path = "Delete_post.php", hasBody = true)
    suspend fun deletePost(@Body request: DeleteRequest): Response<BasicResponse>

    @GET("Load_post.php")
    suspend fun getAllPosts(): Response<PostListResponse>

    @GET("Popular_posts.php")
    suspend fun getPopularPosts(): Response<PostListResponse>

    // 🔹 댓글
    @GET("Load_comment.php")
    suspend fun getComments(@Query("post_id") postId: Int): Response<CommentListResponse>

    @POST("Save_comment.php")
    suspend fun saveComment(@Body request: CommentRequest): Response<BasicResponse>

    @HTTP(method = "DELETE", path = "Delete_comment.php", hasBody = true)
    suspend fun deleteComment(@Body request: DeleteCommentRequest): Response<BasicResponse>

    @PUT("Edit_comment.php")
    suspend fun editComment(@Body request: EditCommentRequest): Response<BasicResponse>

    // 🔹 신고
    @POST("cors.php")
    suspend fun reportPost(@Body reportRequest: ReportRequest): Response<ReportResponse>

    @GET("cors.php")
    suspend fun getReports(): Response<List<Report>>

    @PUT("cors.php")
    suspend fun updateReportStatus(
        @Path("reportId") reportId: Int,
        @Body status: String
    ): Response<ReportResponse>

    // 🔹 좋아요
    @POST("Like_post.php")
    suspend fun likePost(@Body request: LikePostRequest): Response<BasicResponse>

    // 🔹 재료 등록
    @POST("Save_ingredients.php")
    suspend fun registerIngredient(@Body request: com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientRequest): Response<BasicResponse>

    // 🔹 레시피 등록
    @POST("Save_recipe.php")
    suspend fun registerRecipe(@Body request: com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeRequest): Response<BasicResponse>

    // 🔹 레시피 리스트
    @GET("Recipe_list.php")
    suspend fun getRecipeList(@Query("business_id") businessId: Int): Response<com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeListResponse>

    // 🔹 재료 목록 조회
    @GET("Ingredient_list.php")
    suspend fun getIngredientsList(): Response<com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientListResponse>

    // 🔹 마진 요약
    @GET("Recipe_margin_summary.php")
    suspend fun getMarginSummary(@Query("business_id") businessId: Int): Response<com.glowstudio.android.blindsjn.feature.foodcost.model.MarginSummaryResponse>
}
