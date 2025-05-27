package com.glowstudio.android.blindsjn.feature.login.view

/**
 * 회원가입 스크린 로직
 *
 *
 **/

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


import android.util.Log
import com.glowstudio.android.blindsjn.data.network.Network
import com.glowstudio.android.blindsjn.data.model.SignupRequest
import kotlinx.coroutines.launch

//서버 클라이언트 간 회원가입 실행 함수
suspend fun signup(request: SignupRequest): Boolean {
    return try {
        // 서버에 회원가입 요청
        val response = Network.apiService.signup(request)
        Log.d("SignupScreen", "Signup request - phone: ${request.phoneNumber}")
        Log.d("SignupScreen", "Signup response code: ${response.code()}")
        Log.d("SignupScreen", "Signup response body: ${response.body()}")

        // 응답 처리
        if (response.isSuccessful) {
            val result = response.body()
            Log.d("SignupScreen", "Signup response: $result")
            result?.status == "success"
        } else {
            // 오류 로그 출력
            Log.e("SignupScreen", "Signup failed: ${response.errorBody()?.string()}")
            false
        }
    } catch (e: Exception) {
        // 네트워크 오류 처리
        Log.e("SignupScreen", "Error during signup: ${e.message}", e)
        false
    }
}

@Composable
fun SignupScreen(
    onSignupClick: (String, String) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    // 상태 변수
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "회원가입" 텍스트
        Text(
            text = "회원가입",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 전화번호 입력
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
            label = { Text("전화번호") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    "비밀번호가 일치하지 않습니다."
                } else {
                    ""
                }
            },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 비밀번호 확인 입력
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = if (password != confirmPassword) {
                    "비밀번호가 일치하지 않습니다."
                } else {
                    ""
                }
            },
            label = { Text("비밀번호 확인") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 에러 메시지 표시
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 회원가입 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = ""
                    try {
                        val request = SignupRequest(phoneNumber, password)
                        val success = signup(request)
                    if (success) {
                            onSignupClick(phoneNumber, password)
                    } else {
                            errorMessage = "회원가입에 실패했습니다. 다시 시도해주세요."
                        }
                    } catch (e: Exception) {
                        errorMessage = "네트워크 오류가 발생했습니다. 다시 시도해주세요."
                        Log.e("SignupScreen", "Error during signup: ${e.message}", e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank() && password == confirmPassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
            Text("회원가입")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 로그인 화면으로 돌아가기
        TextButton(onClick = onBackToLoginClick) {
            Text("이미 계정이 있으신가요? 로그인")
        }
    }
}
