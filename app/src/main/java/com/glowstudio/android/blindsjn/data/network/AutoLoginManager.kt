package com.glowstudio.android.blindsjn.data.network

/**
 * AutoLoginManager
 * - 사용자 자동 로그인 데이터 관리
 * - 로그인 데이터 저장, 불러오기, 삭제 기능 제공
 */

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore 인스턴스 생성
private val Context.dataStore by preferencesDataStore("user_prefs")

object AutoLoginManager {

    // DataStore 키 정의
    private val AUTO_LOGIN_KEY = booleanPreferencesKey("auto_login") // 자동 로그인 상태
    private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number") // 저장된 전화번호
    private val PASSWORD_KEY = stringPreferencesKey("password") // 저장된 비밀번호

    /**
     * 로그인 정보 저장
     * @param context 앱의 컨텍스트
     * @param phoneNumber 저장할 전화번호
     * @param password 저장할 비밀번호
     * @param autoLogin 자동 로그인 상태 (true: 활성화)
     */
    suspend fun saveLoginInfo(context: Context, phoneNumber: String, password: String, autoLogin: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_LOGIN_KEY] = autoLogin
            if (autoLogin) {
                prefs[PHONE_NUMBER_KEY] = phoneNumber
                prefs[PASSWORD_KEY] = password
            } else {
                prefs.remove(PHONE_NUMBER_KEY)
                prefs.remove(PASSWORD_KEY)
            }
        }
    }

    /**
     * 자동 로그인 활성화 상태 확인
     * @param context 앱의 컨텍스트
     * @return 자동 로그인 활성화 여부 (true/false)
     */
    suspend fun isAutoLoginEnabled(context: Context): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[AUTO_LOGIN_KEY] ?: false
        }.first()
    }

    /**
     * 저장된 사용자 로그인 정보 가져오기
     * @param context 앱의 컨텍스트
     * @return 전화번호와 비밀번호 쌍 (없으면 null 반환)
     */
    suspend fun getSavedCredentials(context: Context): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val phone = prefs[PHONE_NUMBER_KEY]
        val password = prefs[PASSWORD_KEY]
        return if (phone != null && password != null) Pair(phone, password) else null
    }

    /**
     * 로그인 정보 삭제
     * @param context 앱의 컨텍스트
     */
    suspend fun clearLoginInfo(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear() // 모든 저장된 데이터 삭제
        }
    }
}