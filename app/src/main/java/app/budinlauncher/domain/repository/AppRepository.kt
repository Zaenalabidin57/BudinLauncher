package app.budinlauncher.domain.repository

import app.budinlauncher.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getAllApps(): List<AppInfo>
    suspend fun searchApps(query: String): List<AppInfo>
    suspend fun launchApp(packageName: String): Boolean
    fun getAppIcon(packageName: String): android.graphics.drawable.Drawable?
}