package com.glowstudio.android.blindsjn.feature.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.feature.main.model.NavigationState
import com.glowstudio.android.blindsjn.feature.main.model.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    fun updateCurrentRoute(route: String) {
        viewModelScope.launch {
            _navigationState.value = _navigationState.value.copy(currentRoute = route)
        }
    }

    fun navigateToScreen(route: String) {
        viewModelScope.launch {
            _navigationState.value = _navigationState.value.copy(currentRoute = route)
        }
    }
} 