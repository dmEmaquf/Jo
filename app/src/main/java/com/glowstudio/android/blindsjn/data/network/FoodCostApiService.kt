package com.glowstudio.android.blindsjn.data.network

import com.glowstudio.android.blindsjn.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FoodCostApiService {
    @GET("recipes")
    suspend fun getRecipes(
        @Query("user_id") userId: Int
    ): Response<RecipeListResponse>

    @GET("recipes/{id}")
    suspend fun getRecipe(
        @Path("id") id: Int
    ): Response<RecipeResponse>

    @POST("recipes")
    suspend fun createRecipe(
        @Body request: CreateRecipeRequest
    ): Response<RecipeResponse>

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Int,
        @Body request: CreateRecipeRequest
    ): Response<RecipeResponse>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(
        @Path("id") id: Int
    ): Response<RecipeResponse>
} 