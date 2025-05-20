package com.growstudio.app.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object InternalServer {
    private const val BASE_URL = "http://your-api-base-url/" // 실제 API 기본 URL로 변경 필요

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
} 