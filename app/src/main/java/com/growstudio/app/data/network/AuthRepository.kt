package com.growstudio.app.data.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.growstudio.app.data.UserManager
import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {
    suspend fun login(context: Context, phoneNumber: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = InternalServer.api.login(LoginRequest(phoneNumber, password))

                if (response.isSuccessful) {
                    val result: ApiResponse? = response.body()
                    Log.d("AuthRepository", "응답 결과: $result")

                    if (result?.status == "success") {
                        // 사용자 ID 저장
                        result.userId?.let { userId ->
                            UserManager.saveUserId(context, userId)
                        }
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                        }
                        true
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, result?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                        false
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthRepository", "응답 실패: $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    false
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "예외 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "알 수 없는 오류", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }

    suspend fun logout(context: Context) {
        UserManager.clearUserId(context)
    }
} 