package com.glowstudio.android.blindsjn.feature.foodcost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.InternalServer
import com.glowstudio.android.blindsjn.feature.foodcost.model.MarginItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

class MarginListViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<MarginItem>>(emptyList())
    val items: StateFlow<List<MarginItem>> = _items

    fun loadMarginData(businessId: Int) {
        viewModelScope.launch {
            try {
                val response = InternalServer.api.getMarginSummary(businessId)
                if (response.isSuccessful) {
                    response.body()?.let { marginResponse ->
                        if (marginResponse.status == "success") {
                            _items.value = marginResponse.data.recent_sales.map { sale ->
                                MarginItem(
                                    name = sale.title,
                                    price = sale.price,
                                    cost = sale.total_ingredient_price,
                                    marginRate = if (sale.price > 0) ((sale.margin * 100f / sale.price)).toInt() else 0,
                                    color = Color(0xFF1A237E), // 필요시 동적으로 변경
                                    marginColor = Color(0xFF90CAF9)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 에러 처리 필요시 추가
            }
        }
    }
} 