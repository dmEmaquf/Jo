package com.glowstudio.android.blindsjn.feature.board.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.model.Industry
import com.glowstudio.android.blindsjn.data.network.BusinessCertRepository
import com.glowstudio.android.blindsjn.data.network.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostBottomSheetViewModel : ViewModel() {
    private val repository = BusinessCertRepository()

    // 전체 태그 목록
    private val _tags = MutableStateFlow<List<String>>(
        listOf(
            "예비사장님", "알바/직원", "손님", "고민글", "정보", "질문/조언", "후기",
            "초보사장님", "고수사장님"
        )
    )
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    // 선택 가능한 태그 목록 (인증 여부에 따라 이 리스트를 조정)
    private val _enabledTags = MutableStateFlow<List<String>>(
        listOf(
            "예비사장님", "알바/직원", "손님", "고민글", "정보", "질문/조언", "후기"
        )
    )
    val enabledTags: StateFlow<List<String>> = _enabledTags.asStateFlow()

    // 선택된 태그 목록
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    // 업종 목록
    private val _industries = MutableStateFlow<List<Industry>>(emptyList())
    val industries: StateFlow<List<Industry>> = _industries.asStateFlow()

    // 사용자의 인증된 업종
    private val _certifiedIndustry = MutableStateFlow<Industry?>(null)
    val certifiedIndustry: StateFlow<Industry?> = _certifiedIndustry.asStateFlow()

    fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                // 1. 사업자 인증 상태 확인
                val phoneNumber = UserManager.getPhoneNumber(context)
                if (phoneNumber != null) {
                    val isCertified = repository.checkAlreadyCertified(phoneNumber)
                    if (isCertified) {
                        val certification = repository.getBusinessCertification(phoneNumber)
                        if (certification.isSuccessful) {
                            certification.body()?.data?.let { cert ->
                                val response = repository.getIndustries()
                                if (response.isSuccessful) {
                                    response.body()?.data?.let { industries ->
                                        _certifiedIndustry.value = industries.find { it.id == cert.industryId }
                                    }
                                }
                            }
                        }
                    }

                    // 2. 선택 가능한 태그 설정
                    val enabledTags = mutableListOf<String>()
                    // 기본 태그는 항상 활성화
                    enabledTags.addAll(listOf(
                        "예비사장님", "알바/직원", "손님", "고민글", "정보", "질문/조언", "후기"
                    ))

                    // 인증된 사용자는 추가 태그 활성화
                    if (isCertified) {
                        enabledTags.addAll(listOf("초보사장님", "고수사장님"))
                    }

                    _enabledTags.value = enabledTags
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 태그 선택 토글 함수
    fun toggleTag(tag: String) {
        val currentTags = _selectedTags.value.toMutableSet()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _selectedTags.value = currentTags
    }

    // 선택된 태그 초기화
    fun clearSelectedTags() {
        _selectedTags.value = emptySet()
    }

    fun clearSelection() {
        _selectedTags.value = emptySet()
        _certifiedIndustry.value = null
    }

    fun setTags(tags: List<String>, enabledTags: List<String>) {
        _tags.value = tags
        _enabledTags.value = enabledTags
        _selectedTags.value = emptySet()
    }
} 