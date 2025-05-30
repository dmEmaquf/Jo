package com.glowstudio.android.blindsjn.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.data.model.Category
import com.glowstudio.android.blindsjn.data.model.Industry
import com.glowstudio.android.blindsjn.feature.board.model.PostRequest
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import com.glowstudio.android.blindsjn.data.network.CommunityRepository
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen(
    onPostCreated: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToCertification: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedIndustry by remember { mutableStateOf<Industry?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCertificationDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { CommunityRepository() }
    val businessRepository = remember { BusinessCertRepository() }

    // 업종 목록 로드
    var industries by remember { mutableStateOf<List<Industry>>(emptyList()) }
    val userId by UserManager.getUserId(context).collectAsState(initial = -1)
    val phoneNumber = UserManager.getPhoneNumber(context)
    var isCertified by remember { mutableStateOf(false) }
    var certifiedIndustry by remember { mutableStateOf<Industry?>(null) }

    // 사업자 인증 상태 확인
    LaunchedEffect(Unit) {
        try {
            if (phoneNumber != null) {
                isCertified = businessRepository.checkAlreadyCertified(phoneNumber)
                if (isCertified) {
                    val certification = businessRepository.getBusinessCertification(phoneNumber)
                    if (certification.isSuccessful) {
                        certification.body()?.data?.let { cert ->
                            val response = businessRepository.getIndustries()
                            if (response.isSuccessful) {
                                response.body()?.data?.let { loadedIndustries ->
                                    industries = loadedIndustries
                                    certifiedIndustry = loadedIndustries.find { it.id == cert.industryId }
                                }
                            }
                        }
                    }
                } else {
                    // 인증되지 않은 경우에도 업종 목록은 로드
                    val response = businessRepository.getIndustries()
                    if (response.isSuccessful) {
                        response.body()?.data?.let { loadedIndustries ->
                            industries = loadedIndustries
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "인증 정보를 불러오는데 실패했습니다."
        }
    }

    // 초기 카테고리 설정
    LaunchedEffect(isCertified) {
        if (!isCertified) {
            selectedCategory = Category.GENERAL
            selectedIndustry = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "게시글 작성",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 카테고리 선택
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 자유게시판 항목
            item {
                CategoryItem(
                    name = "자유게시판",
                    isSelected = selectedCategory == Category.GENERAL,
                    isEnabled = true,
                    onClick = {
                        selectedCategory = Category.GENERAL
                        selectedIndustry = null
                    }
                )
            }

            // 업종별 게시판 목록
            items(industries) { industry ->
                val isEnabled = isCertified && industry.id == certifiedIndustry?.id
                CategoryItem(
                    name = industry.name,
                    isSelected = selectedCategory == Category.INDUSTRY && selectedIndustry?.id == industry.id,
                    isEnabled = isEnabled,
                    onClick = {
                        if (!isCertified) {
                            showCertificationDialog = true
                            return@CategoryItem
                        }
                        
                        if (industry.id == certifiedIndustry?.id) {
                            selectedCategory = Category.INDUSTRY
                            selectedIndustry = industry
                        } else {
                            showCertificationDialog = true
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 제목 입력
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 내용 입력
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("내용") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            minLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 작성 버튼
        Button(
            onClick = {
                if (title.isEmpty()) {
                    errorMessage = "제목을 입력해주세요"
                    return@Button
                }
                if (content.isEmpty()) {
                    errorMessage = "내용을 입력해주세요"
                    return@Button
                }
                if (selectedCategory == null) {
                    errorMessage = "카테고리를 선택해주세요"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        userId?.let { id ->
                            val request = PostRequest(
                                title = title,
                                content = content,
                                userId = id,
                                category = selectedCategory!!.displayName,
                                industryId = selectedIndustry?.id,
                                phoneNumber = phoneNumber ?: ""
                            )
                            val response = repository.createPost(request)
                            
                            if (response.isSuccessful && response.body()?.status == "success") {
                                onPostCreated()
                            } else {
                                errorMessage = response.body()?.message ?: "게시글 작성에 실패했습니다."
                            }
                        } ?: run {
                            errorMessage = "사용자 정보를 찾을 수 없습니다."
                        }
                    } catch (e: Exception) {
                        errorMessage = "게시글 작성 중 오류가 발생했습니다."
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
                Text("작성하기")
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

    // 사업자 인증 다이얼로그
    if (showCertificationDialog) {
        AlertDialog(
            onDismissRequest = { showCertificationDialog = false },
            title = { Text("사업자 인증 필요") },
            text = { 
                Column {
                    Text("사업자 인증 후 업종을 선택할 수 있습니다")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showCertificationDialog = false
                            onNavigateToCertification()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("사업자 인증하기")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showCertificationDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun CategoryItem(
    name: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = name,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
} 