package com.glowstudio.android.blindsjn.feature.ocr.repository

import android.graphics.Bitmap
import com.glowstudio.android.blindsjn.feature.ocr.model.OcrResult
import kotlinx.coroutines.flow.Flow

class OcrRepositoryImpl : OcrRepository {
    override suspend fun processImage(bitmap: Bitmap): Flow<OcrResult> {
        TODO("Not yet implemented")
    }

    override suspend fun saveOcrResult(result: OcrResult) {
        TODO("Not yet implemented")
    }

    override fun getOcrHistory(): Flow<List<OcrResult>> {
        TODO("Not yet implemented")
    }
} 