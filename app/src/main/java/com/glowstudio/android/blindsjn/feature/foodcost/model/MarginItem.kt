package com.glowstudio.android.blindsjn.feature.foodcost.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier

data class MarginItem(
    val name: String,
    val price: Int,
    val cost: Int,
    val marginRate: Int,
    val color: Color,
    val marginColor: Color,
    val modifier: Modifier = Modifier
) 