package app.budinlauncher

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    companion object {
        private const val PREF = "app.budinlauncher"

        private const val HOME_APPS_NUM = "HOME_APPS_NUM"

        private const val APP_NAME_1 = "APP_NAME_1"
        private const val APP_NAME_2 = "APP_NAME_2"
        private const val APP_NAME_3 = "APP_NAME_3"
        private const val APP_NAME_4 = "APP_NAME_4"
        private const val APP_NAME_5 = "APP_NAME_5"
        private const val APP_NAME_6 = "APP_NAME_6"
        private const val APP_NAME_7 = "APP_NAME_7"
        private const val APP_NAME_8 = "APP_NAME_8"

        private const val APP_PACKAGE_1 = "APP_PACKAGE_1"
        private const val APP_PACKAGE_2 = "APP_PACKAGE_2"
        private const val APP_PACKAGE_3 = "APP_PACKAGE_3"
        private const val APP_PACKAGE_4 = "APP_PACKAGE_4"
        private const val APP_PACKAGE_5 = "APP_PACKAGE_5"
        private const val APP_PACKAGE_6 = "APP_PACKAGE_6"
        private const val APP_PACKAGE_7 = "APP_PACKAGE_7"
        private const val APP_PACKAGE_8 = "APP_PACKAGE_8"

        private const val APP_USER_HANDLE_1 = "APP_USER_HANDLE_1"
        private const val APP_USER_HANDLE_2 = "APP_USER_HANDLE_2"
        private const val APP_USER_HANDLE_3 = "APP_USER_HANDLE_3"
        private const val APP_USER_HANDLE_4 = "APP_USER_HANDLE_4"
        private const val APP_USER_HANDLE_5 = "APP_USER_HANDLE_5"
        private const val APP_USER_HANDLE_6 = "APP_USER_HANDLE_6"
        private const val APP_USER_HANDLE_7 = "APP_USER_HANDLE_7"
        private const val APP_USER_HANDLE_8 = "APP_USER_HANDLE_8"

        // Swipe apps preferences
        private const val APP_NAME_SWIPE_LEFT = "APP_NAME_SWIPE_LEFT"
        private const val APP_NAME_SWIPE_RIGHT = "APP_NAME_SWIPE_RIGHT"
        private const val APP_PACKAGE_SWIPE_LEFT = "APP_PACKAGE_SWIPE_LEFT"
        private const val APP_PACKAGE_SWIPE_RIGHT = "APP_PACKAGE_SWIPE_RIGHT"
        private const val APP_USER_HANDLE_SWIPE_LEFT = "APP_USER_HANDLE_SWIPE_LEFT"
        private const val APP_USER_HANDLE_SWIPE_RIGHT = "APP_USER_HANDLE_SWIPE_RIGHT"

        // Screen time preferences
        private const val SCREEN_TIME_ENABLED = "SCREEN_TIME_ENABLED"
        private const val SCREEN_TIME_LAST_UPDATED = "SCREEN_TIME_LAST_UPDATED"

        // App sorting preferences
        private const val APP_SORTING_MODE = "APP_SORTING_MODE"

        // App usage tracking
        private const val APP_USAGE_PREFIX = "APP_USAGE_"

        // Keyboard auto-show preference
        private const val KEYBOARD_AUTO_SHOW = "KEYBOARD_AUTO_SHOW"

        // Text size preference
        private const val TEXT_SIZE_SCALE = "TEXT_SIZE_SCALE"

        // Alignment preferences
        private const val HOME_ALIGNMENT = "HOME_ALIGNMENT"
        private const val HOME_BOTTOM_ALIGNMENT = "HOME_BOTTOM_ALIGNMENT"

        // Custom app names
        private const val CUSTOM_APP_NAME_PREFIX = "CUSTOM_APP_NAME_"

        // Planetary Menu preferences
        private const val PLANETARY_MENU_ENABLED = "PLANETARY_MENU_ENABLED"
        private const val PLANET_APP_NAME_PREFIX = "PLANET_APP_NAME_"
        private const val PLANET_APP_PACKAGE_PREFIX = "PLANET_APP_PACKAGE_"
        private const val PLANET_APP_USER_HANDLE_PREFIX = "PLANET_APP_USER_HANDLE_"

        // Theme preference
        private const val APP_THEME = "APP_THEME"

        // Show app icons preference
        private const val SHOW_APP_ICONS = "SHOW_APP_ICONS"

        // Thick text preference
        private const val THICK_TEXT = "THICK_TEXT"

        // Navigation style preference
        private const val NAVIGATION_STYLE = "NAVIGATION_STYLE"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    // Home apps number
    var homeAppsNum: Int
        get() = sharedPreferences.getInt(HOME_APPS_NUM, 6)
        set(value) = sharedPreferences.edit().putInt(HOME_APPS_NUM, value).apply()

    fun getAppName(location: Int): String {
        return when (location) {
            1 -> sharedPreferences.getString(APP_NAME_1, "") ?: ""
            2 -> sharedPreferences.getString(APP_NAME_2, "") ?: ""
            3 -> sharedPreferences.getString(APP_NAME_3, "") ?: ""
            4 -> sharedPreferences.getString(APP_NAME_4, "") ?: ""
            5 -> sharedPreferences.getString(APP_NAME_5, "") ?: ""
            6 -> sharedPreferences.getString(APP_NAME_6, "") ?: ""
            7 -> sharedPreferences.getString(APP_NAME_7, "") ?: ""
            8 -> sharedPreferences.getString(APP_NAME_8, "") ?: ""
            else -> ""
        }
    }

    fun getAppPackage(location: Int): String {
        return when (location) {
            1 -> sharedPreferences.getString(APP_PACKAGE_1, "") ?: ""
            2 -> sharedPreferences.getString(APP_PACKAGE_2, "") ?: ""
            3 -> sharedPreferences.getString(APP_PACKAGE_3, "") ?: ""
            4 -> sharedPreferences.getString(APP_PACKAGE_4, "") ?: ""
            5 -> sharedPreferences.getString(APP_PACKAGE_5, "") ?: ""
            6 -> sharedPreferences.getString(APP_PACKAGE_6, "") ?: ""
            7 -> sharedPreferences.getString(APP_PACKAGE_7, "") ?: ""
            8 -> sharedPreferences.getString(APP_PACKAGE_8, "") ?: ""
            else -> ""
        }
    }

    fun getAppUserHandle(location: Int): String {
        return when (location) {
            1 -> sharedPreferences.getString(APP_USER_HANDLE_1, "") ?: ""
            2 -> sharedPreferences.getString(APP_USER_HANDLE_2, "") ?: ""
            3 -> sharedPreferences.getString(APP_USER_HANDLE_3, "") ?: ""
            4 -> sharedPreferences.getString(APP_USER_HANDLE_4, "") ?: ""
            5 -> sharedPreferences.getString(APP_USER_HANDLE_5, "") ?: ""
            6 -> sharedPreferences.getString(APP_USER_HANDLE_6, "") ?: ""
            7 -> sharedPreferences.getString(APP_USER_HANDLE_7, "") ?: ""
            8 -> sharedPreferences.getString(APP_USER_HANDLE_8, "") ?: ""
            else -> ""
        }
    }

    fun setHomeApp(app: AppModel, location: Int) {
        val editor = sharedPreferences.edit()
        when (location) {
            1 -> {
                editor.putString(APP_NAME_1, app.appLabel)
                editor.putString(APP_PACKAGE_1, app.appPackage)
                editor.putString(APP_USER_HANDLE_1, app.userHandle.toString())
            }
            2 -> {
                editor.putString(APP_NAME_2, app.appLabel)
                editor.putString(APP_PACKAGE_2, app.appPackage)
                editor.putString(APP_USER_HANDLE_2, app.userHandle.toString())
            }
            3 -> {
                editor.putString(APP_NAME_3, app.appLabel)
                editor.putString(APP_PACKAGE_3, app.appPackage)
                editor.putString(APP_USER_HANDLE_3, app.userHandle.toString())
            }
            4 -> {
                editor.putString(APP_NAME_4, app.appLabel)
                editor.putString(APP_PACKAGE_4, app.appPackage)
                editor.putString(APP_USER_HANDLE_4, app.userHandle.toString())
            }
            5 -> {
                editor.putString(APP_NAME_5, app.appLabel)
                editor.putString(APP_PACKAGE_5, app.appPackage)
                editor.putString(APP_USER_HANDLE_5, app.userHandle.toString())
            }
            6 -> {
                editor.putString(APP_NAME_6, app.appLabel)
                editor.putString(APP_PACKAGE_6, app.appPackage)
                editor.putString(APP_USER_HANDLE_6, app.userHandle.toString())
            }
            7 -> {
                editor.putString(APP_NAME_7, app.appLabel)
                editor.putString(APP_PACKAGE_7, app.appPackage)
                editor.putString(APP_USER_HANDLE_7, app.userHandle.toString())
            }
            8 -> {
                editor.putString(APP_NAME_8, app.appLabel)
                editor.putString(APP_PACKAGE_8, app.appPackage)
                editor.putString(APP_USER_HANDLE_8, app.userHandle.toString())
            }
        }
        editor.apply()
    }

    // Swipe apps methods
    var appNameSwipeLeft: String
        get() = sharedPreferences.getString(APP_NAME_SWIPE_LEFT, "Camera") ?: "Camera"
        set(value) = sharedPreferences.edit().putString(APP_NAME_SWIPE_LEFT, value).apply()

    var appNameSwipeRight: String
        get() = sharedPreferences.getString(APP_NAME_SWIPE_RIGHT, "Phone") ?: "Phone"
        set(value) = sharedPreferences.edit().putString(APP_NAME_SWIPE_RIGHT, value).apply()

    var appPackageSwipeLeft: String
        get() = sharedPreferences.getString(APP_PACKAGE_SWIPE_LEFT, "") ?: ""
        set(value) = sharedPreferences.edit().putString(APP_PACKAGE_SWIPE_LEFT, value).apply()

    var appPackageSwipeRight: String
        get() = sharedPreferences.getString(APP_PACKAGE_SWIPE_RIGHT, "") ?: ""
        set(value) = sharedPreferences.edit().putString(APP_PACKAGE_SWIPE_RIGHT, value).apply()

    var appUserHandleSwipeLeft: String
        get() = sharedPreferences.getString(APP_USER_HANDLE_SWIPE_LEFT, "") ?: ""
        set(value) = sharedPreferences.edit().putString(APP_USER_HANDLE_SWIPE_LEFT, value).apply()

    var appUserHandleSwipeRight: String
        get() = sharedPreferences.getString(APP_USER_HANDLE_SWIPE_RIGHT, "") ?: ""
        set(value) = sharedPreferences.edit().putString(APP_USER_HANDLE_SWIPE_RIGHT, value).apply()

    // Screen time methods
    var screenTimeEnabled: Boolean
        get() = sharedPreferences.getBoolean(SCREEN_TIME_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(SCREEN_TIME_ENABLED, value).apply()

    var screenTimeLastUpdated: Long
        get() = sharedPreferences.getLong(SCREEN_TIME_LAST_UPDATED, 0L)
        set(value) = sharedPreferences.edit().putLong(SCREEN_TIME_LAST_UPDATED, value).apply()

    // App sorting methods
    var appSortingMode: Int
        get() = sharedPreferences.getInt(APP_SORTING_MODE, 0) // 0 = alphabetical, 1 = frequent usage
        set(value) = sharedPreferences.edit().putInt(APP_SORTING_MODE, value).apply()

    // App usage tracking methods
    fun incrementAppUsage(packageName: String) {
        val key = "$APP_USAGE_PREFIX$packageName"
        val currentCount = sharedPreferences.getInt(key, 0)
        sharedPreferences.edit().putInt(key, currentCount + 1).apply()
    }

    fun getAppUsage(packageName: String): Int {
        val key = "$APP_USAGE_PREFIX$packageName"
        return sharedPreferences.getInt(key, 0)
    }

    // Keyboard auto-show methods
    var keyboardAutoShow: Boolean
        get() = sharedPreferences.getBoolean(KEYBOARD_AUTO_SHOW, true) // Default to true for existing users
        set(value) = sharedPreferences.edit().putBoolean(KEYBOARD_AUTO_SHOW, value).apply()

    // Text size methods
    var textSizeScale: Float
        get() = sharedPreferences.getFloat(TEXT_SIZE_SCALE, 1.0f)
        set(value) = sharedPreferences.edit().putFloat(TEXT_SIZE_SCALE, value).apply()

    // Alignment methods
    var homeAlignment: Int
        get() = sharedPreferences.getInt(HOME_ALIGNMENT, Constants.Alignment.LEFT)
        set(value) = sharedPreferences.edit().putInt(HOME_ALIGNMENT, value).apply()

    var homeBottomAlignment: Boolean
        get() = sharedPreferences.getBoolean(HOME_BOTTOM_ALIGNMENT, false)
        set(value) = sharedPreferences.edit().putBoolean(HOME_BOTTOM_ALIGNMENT, value).apply()

    // Custom app name methods
    fun getCustomAppName(packageName: String, defaultName: String): String {
        val key = "$CUSTOM_APP_NAME_PREFIX$packageName"
        return sharedPreferences.getString(key, defaultName) ?: defaultName
    }

    fun setCustomAppName(packageName: String, name: String) {
        val key = "$CUSTOM_APP_NAME_PREFIX$packageName"
        sharedPreferences.edit().putString(key, name).apply()
    }

    fun removeCustomAppName(packageName: String) {
        val key = "$CUSTOM_APP_NAME_PREFIX$packageName"
        sharedPreferences.edit().remove(key).apply()
    }

    // Theme methods
    var appTheme: Int
        get() = sharedPreferences.getInt(APP_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = sharedPreferences.edit().putInt(APP_THEME, value).apply()

    // Show app icons methods
    var showAppIcons: Boolean
        get() = sharedPreferences.getBoolean(SHOW_APP_ICONS, true)
        set(value) = sharedPreferences.edit().putBoolean(SHOW_APP_ICONS, value).apply()

    // Thick text methods
    var thickText: Boolean
        get() = sharedPreferences.getBoolean(THICK_TEXT, false)
        set(value) = sharedPreferences.edit().putBoolean(THICK_TEXT, value).apply()

    // Get font family based on thick text preference
    fun getFontFamily(): String {
        return if (thickText) "sans-serif-medium" else "sans-serif-light"
    }

    // Navigation style methods
    var navigationStyle: Int
        get() = sharedPreferences.getInt(NAVIGATION_STYLE, Constants.NavigationStyle.PLANET)
        set(value) = sharedPreferences.edit().putInt(NAVIGATION_STYLE, value).apply()

    object Constants {
        object Alignment {
            const val LEFT = 1
            const val CENTER = 2
            const val RIGHT = 3
        }

        object NavigationStyle {
            const val LIST = 0
            const val PLANET = 1
        }
    }
}