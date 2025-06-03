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
import com.glowstudio.android.blindsjn.data.repository.PhoneVerificationRepository
import kotlinx.coroutines.launch

//서버 클라이언트 간 회원가입 실행 함수
suspend fun signup(request: SignupRequest): Result<Boolean> {
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
            
            if (result?.status == "success") {
                Result.success(true)
            } else {
                // 서버에서 보낸 에러 메시지 사용
                Result.failure(Exception(result?.message ?: "회원가입에 실패했습니다."))
            }
        } else {
            // 오류 응답 파싱
            val errorBody = response.errorBody()?.string()
            Log.e("SignupScreen", "Signup failed: $errorBody")
            
            // JSON 파싱
            val errorJson = org.json.JSONObject(errorBody ?: "{}")
            val errorCode = errorJson.optString("errorCode")
            val errorMessage = errorJson.optString("message")
            
            if (errorCode == "DUPLICATE_PHONE") {
                Result.failure(Exception("이미 가입된 전화번호입니다. 로그인을 시도해주세요."))
            } else {
                Result.failure(Exception(errorMessage.ifEmpty { "회원가입에 실패했습니다. 다시 시도해주세요." }))
            }
        }
    } catch (e: Exception) {
        // 네트워크 오류 처리
        Log.e("SignupScreen", "Error during signup: ${e.message}", e)
        Result.failure(e)
    }
}

@Composable
fun SignupScreen(
    onSignupClick: (String, String) -> Unit,
    onBackToLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    // 상태 변수
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var isVerificationSent by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val phoneVerificationRepository = remember { PhoneVerificationRepository() }

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
            modifier = Modifier.fillMaxWidth(),
            enabled = !isVerificationSent
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 인증번호 발송 버튼
        if (!isVerificationSent) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            phoneVerificationRepository.sendVerificationCode(phoneNumber)
                                .onSuccess { response ->
                                    if (response.status == "success") {
                                        isVerificationSent = true
                                        errorMessage = "인증번호가 발송되었습니다."
                                    } else {
                                        errorMessage = response.message
                                    }
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "인증번호 발송에 실패했습니다."
                                }
                        } catch (e: Exception) {
                            errorMessage = "네트워크 오류가 발생했습니다."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && phoneNumber.length >= 10,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("인증번호 발송")
                }
            }
        }

        // 인증번호 입력 필드
        if (isVerificationSent && !isVerified) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it.filter { char -> char.isDigit() } },
                label = { Text("인증번호") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 인증번호 확인 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            phoneVerificationRepository.verifyCode(phoneNumber, verificationCode)
                                .onSuccess { response ->
                                    if (response.status == "success") {
                                        isVerified = true
                                        errorMessage = "인증이 완료되었습니다."
                                    } else {
                                        errorMessage = response.message
                                    }
                                }
                                .onFailure { error ->
                                    errorMessage = error.message ?: "인증에 실패했습니다."
                                }
                        } catch (e: Exception) {
                            errorMessage = "네트워크 오류가 발생했습니다."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && verificationCode.length == 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("인증번호 확인")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 비밀번호 입력
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 비밀번호 확인
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("비밀번호 확인") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = ""
                    try {
                        val request = SignupRequest(
                            phoneNumber = phoneNumber,
                            password = password,
                            verificationCode = verificationCode
                        )
                        signup(request)
                            .onSuccess { success ->
                                if (success) {
                                    onSignupClick(phoneNumber, password)
                                }
                            }
                            .onFailure { e ->
                                errorMessage = e.message ?: "회원가입에 실패했습니다. 다시 시도해주세요."
                            }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "회원가입에 실패했습니다. 다시 시도해주세요."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank() && 
                     password == confirmPassword && isVerified,
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

        // 비밀번호 찾기 링크 추가
        TextButton(onClick = onForgotPasswordClick) {
            Text("비밀번호를 잊어버리셨나요?")
        }
    }
}
