package app.budinlauncher.domain.usecase

import app.budinlauncher.domain.model.AppInfo
import app.budinlauncher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): Flow<Result<List<AppInfo>>> = flow {
        try {
            emit(Result.success(repository.getAllApps()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}