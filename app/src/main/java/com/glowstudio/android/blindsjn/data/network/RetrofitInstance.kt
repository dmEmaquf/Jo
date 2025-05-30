package com.glowstudio.android.blindsjn.data.network

/**
 * URL 서버 통신 객체
 *
 *
 **/

import com.glowstudio.android.blindsjn.data.network.ApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.http.GET
import retrofit2.http.Query

// 네이버 뉴스 응답 모델
data class NaverNewsItem(
    val title: String,
    val originallink: String,
    val link: String,
    val description: String,
    val pubDate: String
)

data class NaverNewsResponse(
    val items: List<NaverNewsItem>
)

interface NaverNewsApiService {
    @GET("v1/search/news.json")
    suspend fun searchNews(
        @Query("query") query: String,
        @Query("display") display: Int = 20,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "date"
    ): Response<NaverNewsResponse>
}

// 공통 네트워크 설정
object NetworkConfig {
    const val INTERNAL_BASE_URL = "http://wonrdc.iptime.org/"
    const val NAVER_BASE_URL = "https://openapi.naver.com/"
    const val PUBLIC_API_BASE_URL = "https://api.odcloud.kr/api/"

    val defaultClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val naverClient by lazy {
        OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Naver-Client-Id", "ztMJBFDCJqlNxnax0Hrj")
                .addHeader("X-Naver-Client-Secret", "GrIMlIGxdu")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    }
}

// 네이버 뉴스 서버용 Retrofit 인스턴스
object NaverNewsServer {
    val apiService: NaverNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.NAVER_BASE_URL)
            .client(NetworkConfig.naverClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverNewsApiService::class.java)
    }
}

// 내부 서버
object InternalServer {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.INTERNAL_BASE_URL)
            .client(NetworkConfig.defaultClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(ApiService::class.java)
    }
}

// 공공 API 서버용 Retrofit 인스턴스
object PublicApiRetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.PUBLIC_API_BASE_URL)
            .client(NetworkConfig.defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}