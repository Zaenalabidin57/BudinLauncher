package app.budinlauncher

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _appList = MutableStateFlow<List<AppModel>>(emptyList())
    val appList: StateFlow<List<AppModel>> = _appList.asStateFlow()

    private val context = application.applicationContext
    private val prefs = Prefs(context)
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    fun loadApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val tempApps = mutableListOf<AppModel>()
                
                for (profile in userManager.userProfiles) {
                    for (activityInfo in launcherApps.getActivityList(null, profile)) {
                        if (activityInfo.applicationInfo.packageName != context.packageName) {
                            tempApps.add(
                                AppModel(
                                    activityInfo.label.toString(),
                                    activityInfo.applicationInfo.packageName,
                                    profile
                                )
                            )
                        }
                    }
                }

                Collections.sort(tempApps) { app1, app2 ->
                    if (prefs.appSortingMode == 1) { // Frequent usage
                        val usage1 = prefs.getAppUsage(app1.appPackage)
                        val usage2 = prefs.getAppUsage(app2.appPackage)
                        when {
                            usage1 != usage2 -> usage2.compareTo(usage1) // Higher usage first
                            else -> app1.appLabel.compareTo(app2.appLabel, ignoreCase = true) // Alphabetical as tiebreaker
                        }
                    } else { // Alphabetical
                        app1.appLabel.compareTo(app2.appLabel, ignoreCase = true)
                    }
                }
                tempApps
            }
            _appList.value = apps
        }
    }
}
