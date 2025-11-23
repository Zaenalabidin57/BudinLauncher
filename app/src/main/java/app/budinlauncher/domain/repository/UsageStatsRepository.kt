package app.budinlauncher.domain.repository

import app.budinlauncher.domain.model.UsageStats
import kotlinx.coroutines.flow.Flow

interface UsageStatsRepository {
    suspend fun getUsageStats(period: UsagePeriod): List<UsageStats>
    suspend fun getAppUsageStats(packageName: String, period: UsagePeriod): UsageStats?
    
    enum class UsagePeriod {
        DAILY, WEEKLY, MONTHLY
    }
}