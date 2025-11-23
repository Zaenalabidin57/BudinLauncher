package app.budinlauncher.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSetting(key: String, defaultValue: String): String
    suspend fun setSetting(key: String, value: String)
    suspend fun getBooleanSetting(key: String, defaultValue: Boolean): Boolean
    suspend fun setBooleanSetting(key: String, value: Boolean)
    fun observeSetting(key: String, defaultValue: String): Flow<String>
    fun observeBooleanSetting(key: String, defaultValue: Boolean): Flow<Boolean>
}