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

    // 전체 태그 목록 (누구나, 질문, 업종 목록)
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    // 선택 가능한 태그 목록
    private val _enabledTags = MutableStateFlow<List<String>>(emptyList())
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
                // 1. 업종 목록 가져오기
                val response = repository.getIndustries()
                if (response.isSuccessful) {
                    response.body()?.data?.let { industries ->
                        _industries.value = industries
                    }
                }

                // 2. 기본 태그 설정 (누구나, 질문)
                val baseTags = listOf("누구나", "질문")
                // 모든 태그를 보이게 설정 (누구나, 질문 + 모든 업종)
                _tags.value = baseTags + _industries.value.map { it.name }

                // 3. 사업자 인증 상태 확인
                val phoneNumber = UserManager.getPhoneNumber(context)
                if (phoneNumber != null) {
                    val certification = repository.getBusinessCertification(phoneNumber)
                    if (certification.isSuccessful) {
                        certification.body()?.data?.let { cert ->
                            _certifiedIndustry.value = _industries.value.find { it.id == cert.industryId }
                        }
                    }
                }

                // 4. 선택 가능한 태그 설정
                val enabledTags = mutableListOf<String>()
                enabledTags.addAll(baseTags) // 누구나, 질문은 항상 활성화

                // 인증된 업종이 있으면 해당 업종만 활성화
                _certifiedIndustry.value?.let { industry ->
                    enabledTags.add(industry.name)
                }
                _enabledTags.value = enabledTags
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun toggleTag(tag: String) {
        val currentTags = _selectedTags.value.toMutableSet()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.clear() // 다른 태그 선택 해제
            currentTags.add(tag)
        }
        _selectedTags.value = currentTags
    }

    fun clearSelection() {
        _selectedTags.value = emptySet()
    }
} 