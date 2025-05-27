package com.glowstudio.android.blindsjn.data.network

import com.glowstudio.android.blindsjn.data.model.*
import retrofit2.Response

class FoodCostRepository {
    private val apiService = Network.foodCostApiService

    suspend fun getRecipes(userId: Int): Response<RecipeListResponse> {
        return apiService.getRecipes(userId)
    }

    suspend fun getRecipe(id: Int): Response<RecipeResponse> {
        return apiService.getRecipe(id)
    }

    suspend fun createRecipe(request: CreateRecipeRequest): Response<RecipeResponse> {
        return apiService.createRecipe(request)
    }

    suspend fun updateRecipe(id: Int, request: CreateRecipeRequest): Response<RecipeResponse> {
        return apiService.updateRecipe(id, request)
    }

    suspend fun deleteRecipe(id: Int): Response<RecipeResponse> {
        return apiService.deleteRecipe(id)
    }
} 