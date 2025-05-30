package com.glowstudio.android.blindsjn.feature.foodcost.repository

import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.BasicResponse
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeRequest
import com.glowstudio.android.blindsjn.feature.foodcost.model.Recipe
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeListResponse
import retrofit2.Response

object RecipeRepository {
    suspend fun registerRecipe(request: RecipeRequest): Response<ApiResponse<BasicResponse>> {
        return com.glowstudio.android.blindsjn.data.network.InternalServer.api.registerRecipe(request)
    }
    
    suspend fun getRecipeList(businessId: Int): Response<ApiResponse<List<Recipe>>> {
        return com.glowstudio.android.blindsjn.data.network.InternalServer.api.getRecipeList(businessId)
    }
} 