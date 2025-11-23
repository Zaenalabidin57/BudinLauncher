package app.budinlauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.budinlauncher.domain.model.AppInfo
import app.budinlauncher.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val context: Context,
    private val packageManager: PackageManager
) : AppRepository {

    override suspend fun getAllApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        
        resolveInfos.mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = packageManager.getApplicationIcon(appInfo)
                
                AppInfo(
                    packageName = packageName,
                    label = label,
                    icon = icon,
                    lastUpdateTime = packageManager.getPackageInfo(packageName, 0).lastUpdateTime,
                    firstInstallTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime,
                    versionName = packageManager.getPackageInfo(packageName, 0).versionName,
                    versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageManager.getPackageInfo(packageName, 0).longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
                    },
                    isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }

    override suspend fun searchApps(query: String): List<AppInfo> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            getAllApps()
        } else {
            getAllApps().filter { app ->
                app.label.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                context.startActivity(it)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun getAppIcon(packageName: String): android.graphics.drawable.Drawable? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationIcon(appInfo)
        } catch (e: Exception) {
            null
        }
    }
}