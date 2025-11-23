package app.budinlauncher.data.repository

import android.content.Context
import android.content.SharedPreferences
import app.budinlauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("budin_launcher_prefs", Context.MODE_PRIVATE)

    private val settingChangeChannels = mutableMapOf<String, Channel<String>>()
    private val booleanSettingChangeChannels = mutableMapOf<String, Channel<Boolean>>()

    override suspend fun getSetting(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override suspend fun setSetting(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
        settingChangeChannels[key]?.trySend(value)
    }

    override suspend fun getBooleanSetting(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override suspend fun setBooleanSetting(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        booleanSettingChangeChannels[key]?.trySend(value)
    }

    override fun observeSetting(key: String, defaultValue: String): Flow<String> {
        val channel = settingChangeChannels.getOrPut(key) { Channel() }
        return channel.receiveAsFlow()
    }

    override fun observeBooleanSetting(key: String, defaultValue: Boolean): Flow<Boolean> {
        val channel = booleanSettingChangeChannels.getOrPut(key) { Channel() }
        return channel.receiveAsFlow()
    }
}