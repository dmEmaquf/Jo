package com.glowstudio.android.blindsjn.feature.foodcost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.InternalServer
import com.glowstudio.android.blindsjn.feature.foodcost.model.*
import com.glowstudio.android.blindsjn.feature.foodcost.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {
    private val _registerResult = MutableStateFlow<String?>(null)
    val registerResult: StateFlow<String?> = _registerResult

    private val _recipeList = MutableStateFlow<List<Recipe>>(emptyList())
    val recipeList: StateFlow<List<Recipe>> = _recipeList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun registerRecipe(title: String, price: Long, businessId: Int, ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            try {
                val request = RecipeRequest(title, price, businessId, ingredients)
                val response = RecipeRepository.registerRecipe(request)
                if (response.isSuccessful) {
                    _registerResult.value = response.body()?.message ?: "등록 성공"
                } else {
                    _registerResult.value = "등록 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _registerResult.value = "에러: ${e.message}"
            }
        }
    }

    fun getRecipeList(businessId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = InternalServer.api.getRecipeList(businessId)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _recipeList.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "레시피 목록을 불러오는데 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _registerResult.value = null
    }
} 