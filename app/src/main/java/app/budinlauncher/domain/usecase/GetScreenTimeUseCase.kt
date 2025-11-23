package app.budinlauncher.domain.usecase

import app.budinlauncher.domain.model.UsageStats
import app.budinlauncher.domain.repository.UsageStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetScreenTimeUseCase @Inject constructor(
    private val repository: UsageStatsRepository
) {
    suspend operator fun invoke(period: UsageStatsRepository.UsagePeriod = UsageStatsRepository.UsagePeriod.DAILY): Flow<Result<List<UsageStats>>> = flow {
        try {
            emit(Result.success(repository.getUsageStats(period)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}