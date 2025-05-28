package com.glowstudio.android.blindsjn.feature.foodcost.repository

import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeRequest
import com.glowstudio.android.blindsjn.feature.foodcost.model.Recipe
import com.glowstudio.android.blindsjn.data.model.BasicResponse
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeListResponse
import retrofit2.Response

object RecipeRepository {
    suspend fun registerRecipe(request: RecipeRequest): Response<BasicResponse> {
        return com.glowstudio.android.blindsjn.data.network.InternalServer.api.registerRecipe(request)
    }
    
    suspend fun getRecipeList(businessId: Int): Response<RecipeListResponse> {
        return com.glowstudio.android.blindsjn.data.network.InternalServer.api.getRecipeList(businessId)
    }
} 