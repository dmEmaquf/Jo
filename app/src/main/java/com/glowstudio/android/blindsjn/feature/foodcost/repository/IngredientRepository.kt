package com.glowstudio.android.blindsjn.feature.foodcost.repository

import com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientRequest
import com.glowstudio.android.blindsjn.data.model.BasicResponse
import retrofit2.Response

object IngredientRepository {
    suspend fun registerIngredient(request: IngredientRequest): Response<BasicResponse> {
        return com.glowstudio.android.blindsjn.data.network.InternalServer.api.registerIngredient(request)
    }
} 