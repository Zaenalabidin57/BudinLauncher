package app.budinlauncher.domain.model

data class UsageStats(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val launchCount: Int = 0
)