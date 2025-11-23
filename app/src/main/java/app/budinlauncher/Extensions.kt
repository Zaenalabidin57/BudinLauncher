package app.budinlauncher

import android.app.AppOpsManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions and utilities for the BudinLauncher app
 */

/**
 * Checks if usage access permission is granted for the app
 */
fun Context.appUsagePermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                packageName
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            // For API levels below 29, use the deprecated method
            try {
                val method = appOpsManager.javaClass.getMethod("checkOpNoThrow", String::class.java, Int::class.java, String::class.java)
                val result = method.invoke(appOpsManager, "android:get_usage_stats", android.os.Process.myUid(), packageName)
                result == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                false
            }
        }
    } else {
        true
    }
}

/**
 * Formats time spent in milliseconds to a human-readable string
 */
fun Context.formattedTimeSpent(timeSpent: Long): String {
    val seconds = timeSpent / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        timeSpent == 0L -> "0m"
        hours > 0 -> getString(
            R.string.time_spent_hour,
            hours.toString(),
            remainingMinutes.toString()
        )
        minutes > 0 -> getString(R.string.time_spent_minute, minutes.toString())
        else -> "0m"
    }
}

/**
 * Converts a timestamp to midnight of that day
 */
fun Long.convertEpochToMidnight(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Checks if a timestamp has been a certain number of minutes ago
 */
fun Long.hasBeenMinutes(minutes: Int): Boolean {
    val currentTime = System.currentTimeMillis()
    val diffMinutes = (currentTime - this) / (1000 * 60)
    return diffMinutes >= minutes
}

/**
 * Checks if a package is installed on the device
 */
fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Converts dp to pixels
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}