package com.glowstudio.android.blindsjn.feature.ocr.repository

import android.graphics.Bitmap
import com.glowstudio.android.blindsjn.feature.ocr.model.OcrResult
import kotlinx.coroutines.flow.Flow

interface OcrRepository {
    suspend fun processImage(bitmap: Bitmap): Flow<OcrResult>
    suspend fun saveOcrResult(result: OcrResult)
    fun getOcrHistory(): Flow<List<OcrResult>>
} 