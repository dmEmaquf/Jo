package com.glowstudio.android.blindsjn.feature.foodcost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.InternalServer
import com.glowstudio.android.blindsjn.feature.foodcost.model.Ingredient
import com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IngredientViewModel : ViewModel() {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients

    private val _registerResult = MutableStateFlow<String?>(null)
    val registerResult: StateFlow<String?> = _registerResult

    fun loadIngredients() {
        viewModelScope.launch {
            try {
                val response = InternalServer.api.getIngredientsList()
                if (response.isSuccessful && response.body()?.status == "success") {
                    _ingredients.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error appropriately
                e.printStackTrace()
            }
        }
    }

    fun registerIngredient(request: IngredientRequest) {
        viewModelScope.launch {
            try {
                val response = InternalServer.api.registerIngredient(request)
                if (response.isSuccessful) {
                    _registerResult.value = "재료가 성공적으로 등록되었습니다."
                    loadIngredients() // 목록 새로고침
                } else {
                    _registerResult.value = "등록 실패: ${response.message()}"
                }
            } catch (e: Exception) {
                _registerResult.value = "에러: ${e.message}"
            }
        }
    }

    fun clearResult() {
        _registerResult.value = null
    }
} 