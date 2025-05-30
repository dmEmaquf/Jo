package com.glowstudio.android.blindsjn.feature.ocr.model

data class OcrResult(
    val text: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
) 