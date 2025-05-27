package com.glowstudio.android.blindsjn.feature.board.model

import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("id") val id: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String = "PENDING", // PENDING, REVIEWED, REJECTED
    @SerializedName("created_at") val createdAt: String
)

data class ReportRequest(
    @SerializedName("post_id") val postId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reason") val reason: String
)

data class ReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)