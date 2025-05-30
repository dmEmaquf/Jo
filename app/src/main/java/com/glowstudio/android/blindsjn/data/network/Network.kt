package com.glowstudio.android.blindsjn.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.glowstudio.android.blindsjn.data.model.*
import com.glowstudio.android.blindsjn.feature.board.model.*
import com.glowstudio.android.blindsjn.feature.foodcost.model.Recipe
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeRequest
import com.glowstudio.android.blindsjn.feature.foodcost.model.Ingredient
import com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientRequest
import com.glowstudio.android.blindsjn.feature.foodcost.model.MarginData
import com.glowstudio.android.blindsjn.feature.paymanagement.repository.PayManagementApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.ResponseBody
import java.io.IOException

// ✅ 네트워크 상태 확인 함수
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

object Network {
    private const val BASE_URL = "http://wonrdc.iptime.org/"
    private const val TIMEOUT_SECONDS = 30L

    private val responseInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val responseBody = response.body?.string()
        
        // Extract JSON from response if it contains HTML warnings
        val jsonResponse = responseBody?.let { body ->
            val jsonStart = body.indexOf('{')
            val jsonEnd = body.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                body.substring(jsonStart, jsonEnd + 1)
            } else {
                body
            }
        } ?: "{}"

        // Create new response with cleaned body
        response.newBuilder()
            .body(ResponseBody.create(response.body?.contentType(), jsonResponse))
            .build()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(responseInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val foodCostApiService: FoodCostApiService = retrofit.create(FoodCostApiService::class.java)
    val payManagementApiService: PayManagementApi = retrofit.create(PayManagementApi::class.java)
}

// ✅ Retrofit API 인터페이스
interface ApiService {
    // 🔹 회원가입 / 로그인
    @POST("signup.php")
    suspend fun signup(@Body request: SignupRequest): Response<ApiResponse<BasicResponse>>

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // 🔹 게시글
    @GET("Load_post.php")
    suspend fun getAllPosts(): Response<ApiResponse<List<Post>>>

    @GET("Load_post.php")
    suspend fun getPostsByIndustry(@Query("industry_id") industryId: Int): Response<ApiResponse<List<Post>>>

    @GET("Get_post_by_id.php")
    suspend fun getPostById(@Query("post_id") postId: Int): Response<ApiResponse<Post>>

    @POST("Save_post.php")
    suspend fun savePost(@Body request: PostRequest): Response<ApiResponse<BasicResponse>>

    @POST("Edit_post.php")
    suspend fun editPost(@Body request: EditPostRequest): Response<ApiResponse<BasicResponse>>

    @POST("Delete_post.php")
    suspend fun deletePost(@Body request: DeleteRequest): Response<ApiResponse<BasicResponse>>

    @GET("Popular_posts.php")
    suspend fun getPopularPosts(): Response<ApiResponse<List<Post>>>

    // 🔹 댓글
    @GET("Load_comment.php")
    suspend fun getComments(@Query("post_id") postId: Int): Response<ApiResponse<List<Comment>>>

    @POST("Save_comment.php")
    suspend fun saveComment(@Body request: CommentRequest): Response<ApiResponse<BasicResponse>>

    @POST("Edit_comment.php")
    suspend fun editComment(@Body request: EditCommentRequest): Response<ApiResponse<BasicResponse>>

    @POST("Delete_comment.php")
    suspend fun deleteComment(@Body request: DeleteCommentRequest): Response<ApiResponse<BasicResponse>>

    // 🔹 좋아요
    @POST("Toggle_like.php")
    suspend fun toggleLike(@Body request: LikePostRequest): Response<LikeResponse>

    // 🔹 신고
    @POST("cors.php")
    suspend fun reportPost(@Body request: ReportRequest): Response<ReportResponse>

    // 🔹 산업별 게시판
    @GET("get_industries.php")
    suspend fun getIndustries(): Response<ApiResponse<List<Industry>>>

    // 🔹 사업자 인증
    @GET("check_business.php")
    suspend fun checkBusinessCertification(@Query("business_number") businessNumber: String): Response<ApiResponse<BusinessCertificationResponse>>

    @POST("business_certification.php")
    suspend fun saveBusinessCertification(@Body request: BusinessCertificationRequest): Response<ApiResponse<BusinessCertificationResponse>>

    @GET("get_business.php")
    suspend fun getBusinessCertification(@Query("phone_number") phoneNumber: String): Response<ApiResponse<BusinessCertificationResponse>>

    // 🔹 레시피
    @POST("Save_recipe.php")
    suspend fun registerRecipe(@Body request: RecipeRequest): Response<ApiResponse<BasicResponse>>

    @GET("Recipe_list.php")
    suspend fun getRecipeList(@Query("business_id") businessId: Int): Response<ApiResponse<List<Recipe>>>

    // 🔹 재료
    @GET("Ingredient_list.php")
    suspend fun getIngredientsList(): Response<ApiResponse<List<Ingredient>>>

    @POST("Save_ingredients.php")
    suspend fun registerIngredient(@Body request: IngredientRequest): Response<ApiResponse<BasicResponse>>

    // 🔹 마진
    @GET("Recipe_margin_summary.php")
    suspend fun getMarginSummary(@Query("business_id") businessId: Int): Response<ApiResponse<MarginData>>
} 