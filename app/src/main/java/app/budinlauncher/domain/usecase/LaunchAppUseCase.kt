package app.budinlauncher.domain.usecase

import app.budinlauncher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LaunchAppUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(packageName: String): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.success(repository.launchApp(packageName)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}