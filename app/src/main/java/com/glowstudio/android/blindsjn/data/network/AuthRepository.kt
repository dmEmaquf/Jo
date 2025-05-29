package com.glowstudio.android.blindsjn.data.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.glowstudio.android.blindsjn.data.model.ApiResponse
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import com.glowstudio.android.blindsjn.data.model.LoginResponse
import com.glowstudio.android.blindsjn.data.network.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

object AuthRepository {
    private const val TAG = "AuthRepository"

    suspend fun login(context: Context, phoneNumber: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isNetworkAvailable(context)) {
                    Log.e(TAG, "네트워크 연결 없음")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext false
                }

                if (phoneNumber.isEmpty() || password.isEmpty()) {
                    Log.e(TAG, "전화번호 또는 비밀번호가 비어있음")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "전화번호와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext false
                }

                Log.d(TAG, "로그인 시도 - 전화번호: $phoneNumber, 비밀번호 길이: ${password.length}")

                val request = LoginRequest(phoneNumber, password)
                Log.d(TAG, "요청 데이터: $request")
                
                val response = InternalServer.api.login(request)
                Log.d(TAG, "서버 응답 코드: ${response.code()}")
                Log.d(TAG, "서버 응답 헤더: ${response.headers()}")
                Log.d(TAG, "서버 응답 본문: ${response.body()}")
                Log.d(TAG, "서버 에러 본문: ${response.errorBody()?.string()}")
                
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        if (loginResponse.status == "success") {
                            UserManager.saveUserId(context, loginResponse.user_id)
                            UserManager.savePhoneNumber(context, phoneNumber)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                            }
                            return@withContext true
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "서버 오류 - 코드: ${response.code()}, 메시지: $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "서버 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                return@withContext false
            } catch (e: IOException) {
                Log.e(TAG, "네트워크 오류: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "네트워크 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP 예외: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "서버 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "로그인 실패", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
                return@withContext false
            }
        }
    }
}
