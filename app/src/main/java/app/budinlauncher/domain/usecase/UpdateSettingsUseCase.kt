package app.budinlauncher.domain.usecase

import app.budinlauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun updateStringSetting(key: String, value: String): Flow<Result<Unit>> = flow {
        try {
            repository.setSetting(key, value)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun updateBooleanSetting(key: String, value: Boolean): Flow<Result<Unit>> = flow {
        try {
            repository.setBooleanSetting(key, value)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}