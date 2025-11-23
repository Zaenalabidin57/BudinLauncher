package app.budinlauncher.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import app.budinlauncher.domain.model.UsageStats
import app.budinlauncher.domain.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsRepositoryImpl(
    private val context: Context
) : UsageStatsRepository {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override suspend fun getUsageStats(period: UsageStatsRepository.UsagePeriod): List<UsageStats> = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val startTime = when (period) {
                UsageStatsRepository.UsagePeriod.DAILY -> currentTime - (24 * 60 * 60 * 1000)
                UsageStatsRepository.UsagePeriod.WEEKLY -> currentTime - (7 * 24 * 60 * 60 * 1000)
                UsageStatsRepository.UsagePeriod.MONTHLY -> currentTime - (30L * 24 * 60 * 60 * 1000)
            }

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                currentTime
            )

            usageStats.mapNotNull { stats ->
                if (stats.totalTimeInForeground > 0) {
                    val packageName = stats.packageName
                    val appName = try {
                        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                        context.packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        packageName
                    }
                    
                    UsageStats(
                        packageName = packageName,
                        appName = appName,
                        totalTimeInForeground = stats.totalTimeInForeground,
                        lastTimeUsed = stats.lastTimeUsed,
                        launchCount = stats.lastTimeUsed.toInt()
                    )
                } else null
            }.sortedByDescending { it.totalTimeInForeground }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    override suspend fun getAppUsageStats(packageName: String, period: UsageStatsRepository.UsagePeriod): UsageStats? = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val startTime = when (period) {
                UsageStatsRepository.UsagePeriod.DAILY -> currentTime - (24 * 60 * 60 * 1000)
                UsageStatsRepository.UsagePeriod.WEEKLY -> currentTime - (7 * 24 * 60 * 60 * 1000)
                UsageStatsRepository.UsagePeriod.MONTHLY -> currentTime - (30L * 24 * 60 * 60 * 1000)
            }

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                currentTime
            ).find { it.packageName == packageName }

            usageStats?.let { stats ->
                val appName = try {
                    val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                    context.packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }
                
                UsageStats(
                    packageName = packageName,
                    appName = appName,
                    totalTimeInForeground = stats.totalTimeInForeground,
                    lastTimeUsed = stats.lastTimeUsed,
                    launchCount = stats.lastTimeUsed.toInt()
                )
            }
        } catch (e: SecurityException) {
            null
        }
    }
}