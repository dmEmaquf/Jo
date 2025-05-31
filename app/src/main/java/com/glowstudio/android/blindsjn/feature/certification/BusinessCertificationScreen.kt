package com.glowstudio.android.blindsjn.feature.certification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.glowstudio.android.blindsjn.data.model.BusinessCertificationRequest
import com.glowstudio.android.blindsjn.data.model.Industry
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import com.glowstudio.android.blindsjn.data.network.UserManager
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCertificationScreen(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var businessNumber by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf<Industry?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var industries by remember { mutableStateOf<List<Industry>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BusinessCertRepository() }

    // 업종 목록 로드
    LaunchedEffect(Unit) {
        try {
            val response = repository.getIndustries()
            if (response.isSuccessful) {
                response.body()?.data?.let { data ->
                    industries = data
                }
            }
        } catch (e: Exception) {
            errorMessage = "업종 목록을 불러오는데 실패했습니다."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "사업자 인증",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 사업자 등록번호 입력
        OutlinedTextField(
            value = businessNumber,
            onValueChange = { businessNumber = it },
            label = { Text("사업자 등록번호") },
            placeholder = { Text("사업자 등록번호를 입력하세요") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 업종 선택
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedIndustry?.name ?: "업종을 선택하세요",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                industries.forEach { industry ->
                    DropdownMenuItem(
                        text = { Text(industry.name) },
                        onClick = {
                            selectedIndustry = industry
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 인증 버튼
        Button(
            onClick = {
                if (businessNumber.isEmpty()) {
                    errorMessage = "사업자 등록번호를 입력해주세요"
                    return@Button
                }
                if (selectedIndustry == null) {
                    errorMessage = "업종을 선택해주세요"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        // 1. 이미 인증된 번호인지 확인
                        val (isAlreadyCertified, message) = repository.checkAlreadyCertified(UserManager.getPhoneNumber(context) ?: "", businessNumber)
                        if (isAlreadyCertified) {
                            errorMessage = message
                            return@launch
                        }

                        // 2. 사업자 등록번호 진위확인
                        val isValid = repository.checkBusinessNumberValidity(businessNumber)
                        if (isValid == null) {
                            errorMessage = "유효하지 않은 사업자 등록번호입니다."
                            return@launch
                        }

                        // 3. 사업자 인증 정보 저장
                        val request = BusinessCertificationRequest(
                            phoneNumber = UserManager.getPhoneNumber(context) ?: "",
                            businessNumber = businessNumber,
                            industryId = selectedIndustry?.id ?: 0
                        )
                        val response = repository.saveBusinessCertification(request)
                        
                        if (response.isSuccessful && response.body()?.status == "success") {
                            onConfirm()
                        } else {
                            errorMessage = response.body()?.message ?: "인증에 실패했습니다."
                        }
                    } catch (e: Exception) {
                        errorMessage = "인증 중 오류가 발생했습니다."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("인증하기")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
