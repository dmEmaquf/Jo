package com.glowstudio.android.blindsjn.feature.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen(
    onPostCreated: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedIndustry by remember { mutableStateOf<Industry?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isIndustryExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { CommunityRepository() }
    val businessRepository = remember { BusinessCertRepository() }

    // 업종 목록 로드
    var industries by remember { mutableStateOf<List<Industry>>(emptyList()) }
    val userId by UserManager.getUserId(context).collectAsState(initial = -1)
    val phoneNumber = UserManager.getPhoneNumber(context)

    LaunchedEffect(Unit) {
        try {
            val response = businessRepository.getIndustries()
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
            text = "게시글 작성",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 카테고리 선택
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory?.displayName ?: "카테고리를 선택하세요",
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
                Category.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            selectedCategory = category
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 업종 선택 (업종별 게시판인 경우에만 표시)
        if (selectedCategory == Category.INDUSTRY) {
            ExposedDropdownMenuBox(
                expanded = isIndustryExpanded,
                onExpandedChange = { isIndustryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIndustry?.name ?: "업종을 선택하세요",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIndustryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isIndustryExpanded,
                    onDismissRequest = { isIndustryExpanded = false }
                ) {
                    industries.forEach { industry ->
                        DropdownMenuItem(
                            text = { Text(industry.name) },
                            onClick = {
                                selectedIndustry = industry
                                isIndustryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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
                if (selectedCategory == Category.INDUSTRY && selectedIndustry == null) {
                    errorMessage = "업종을 선택해주세요"
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        // 업종별 게시판인 경우 사업자 인증 확인
                        if (selectedCategory == Category.INDUSTRY) {
                            val isCertified = businessRepository.checkAlreadyCertified(phoneNumber ?: "")
                            if (!isCertified) {
                                errorMessage = "업종별 게시판은 사업자 인증이 필요합니다."
                                return@launch
                            }

                            // 인증된 업종과 게시글 작성하려는 업종이 일치하는지 확인
                            val certification = businessRepository.getBusinessCertification(phoneNumber ?: "").body()?.data
                            if (certification?.industryId != selectedIndustry?.id) {
                                errorMessage = "인증된 업종의 게시판에만 글을 작성할 수 있습니다."
                                return@launch
                            }
                        }

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
} 