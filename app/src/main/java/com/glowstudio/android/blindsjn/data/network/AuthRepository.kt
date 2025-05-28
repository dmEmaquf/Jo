package com.glowstudio.android.blindsjn.data.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

object AuthRepository {

    suspend fun login(context: Context, phoneNumber: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = InternalServer.api.login(LoginRequest(phoneNumber, password))

                if (response.isSuccessful) {
                    val result: ApiResponse? = response.body()
                    Log.d("AuthRepository", "응답 결과: $result")

                    return@withContext if (result?.status == "success" && result.userId != null) {
                        // ✅ userId 저장 등 필요한 처리
                        val userId = result.userId
                        Log.d("AuthRepository", "로그인 성공 - userId: $userId")

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
            } catch (e: IOException) {
                Log.e("AuthRepository", "네트워크 오류: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
                false
            } catch (e: HttpException) {
                Log.e("AuthRepository", "HTTP 예외: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
                false
            } catch (e: Exception) {
                Log.e("AuthRepository", "예외 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "알 수 없는 오류", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }
}
