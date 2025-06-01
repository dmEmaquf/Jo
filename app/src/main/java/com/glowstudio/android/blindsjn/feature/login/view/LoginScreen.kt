package com.glowstudio.android.blindsjn.feature.login.view

/**
 * 로그인 스크린 로직
 *
 * TODO: 자동로그인 체크 박스
 **/

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import com.glowstudio.android.blindsjn.data.network.Network
import com.glowstudio.android.blindsjn.R
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.login.LoginViewModel
import com.glowstudio.android.blindsjn.ui.components.common.AutoLoginRow
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import com.glowstudio.android.blindsjn.ui.components.common.CommonTextField

// 로그인 함수 (서버 통신)
suspend fun login(phoneNumber: String, password: String): Boolean {
    val request = LoginRequest(phoneNumber, password)
    val response = Network.apiService.login(request)

    return if (response.isSuccessful) {
        val result = response.body()
        Log.d("LoginScreen", "Login result: $result")
        result?.status == "success"
    } else {
        Log.e("LoginScreen", "Error: ${response.errorBody()?.string()}")
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginClick: (Boolean) -> Unit,
    onSignupClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // onLoginSuccess 콜백 설정
    LaunchedEffect(Unit) {
        viewModel.setOnLoginSuccess { success ->
            if (success) {
                onLoginClick(true)
            }
        }
    }

    // 자동 로그인 체크
    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.info_1),
                contentDescription = "Login Image",
                contentScale = ContentScale.FillBounds   ,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 환영 메시지
        Text(
            text = "어서오세요, 사장님!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 입력 필드들을 담을 Column
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 전화번호 입력
            CommonTextField(
                value = uiState.phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = "전화번호",
                placeholder = "전화번호를 입력하세요",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            CommonTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = "비밀번호",
                placeholder = "비밀번호를 입력하세요",
                isPassword = true
            )

            // 자동 로그인과 비밀번호 찾기
            AutoLoginRow(
                autoLoginEnabled = uiState.autoLoginEnabled,
                onAutoLoginChange = { viewModel.updateAutoLogin(it) },
                onForgotPasswordClick = onForgotPasswordClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인 버튼
            CommonButton(
                text = "로그인",
                onClick = {
                    viewModel.login(context, uiState.phoneNumber, uiState.password)
                },
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입 안내
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "계정이 없으신가요? ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onSignupClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "회원가입",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 팝업들
        if (uiState.showEmptyFieldsPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissEmptyFieldsPopup() },
                title = { Text("입력 오류") },
                text = { Text("전화번호와 비밀번호를 입력해주세요.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissEmptyFieldsPopup() }) {
                        Text("확인")
                    }
                }
            )
        }

        if (uiState.showInvalidCredentialsPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissInvalidCredentialsPopup() },
                title = { Text("로그인 실패") },
                text = { Text("전화번호 또는 비밀번호가 올바르지 않습니다.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissInvalidCredentialsPopup() }) {
                        Text("확인")
                    }
                }
            )
        }

        if (uiState.showNetworkErrorPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissNetworkErrorPopup() },
                title = { Text("네트워크 오류") },
                text = { Text("인터넷 연결이 필요합니다. 연결 상태를 확인해주세요.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissNetworkErrorPopup() }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = {},
        onSignupClick = {},
        onForgotPasswordClick = {}
    )
}