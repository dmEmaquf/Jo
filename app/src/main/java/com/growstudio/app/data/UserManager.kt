package com.growstudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

object UserManager {
    private val USER_ID_KEY = intPreferencesKey("user_id")

    suspend fun saveUserId(context: Context, userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getUserId(context: Context): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun clearUserId(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
} 