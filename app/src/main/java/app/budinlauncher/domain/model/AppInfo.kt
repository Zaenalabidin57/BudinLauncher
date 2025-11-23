package app.budinlauncher.domain.model

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable,
    val lastUpdateTime: Long = 0L,
    val firstInstallTime: Long = 0L,
    val versionName: String? = null,
    val versionCode: Long = 0L,
    val isSystemApp: Boolean = false
)