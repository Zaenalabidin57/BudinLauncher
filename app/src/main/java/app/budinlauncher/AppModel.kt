package app.budinlauncher

import android.os.UserHandle

data class AppModel(
    val appLabel: String,
    val appPackage: String,
    val userHandle: UserHandle
) : Comparable<AppModel> {
    override fun compareTo(other: AppModel): Int {
        return this.appLabel.compareTo(other.appLabel, ignoreCase = true)
    }
}
