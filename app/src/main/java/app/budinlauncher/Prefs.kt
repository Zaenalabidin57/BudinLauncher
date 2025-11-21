package app.budinlauncher

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    companion object {
        private const val PREF = "app.budinlauncher"

        private const val APP_NAME_1 = "APP_NAME_1"
        private const val APP_NAME_2 = "APP_NAME_2"
        private const val APP_NAME_3 = "APP_NAME_3"
        private const val APP_NAME_4 = "APP_NAME_4"
        private const val APP_NAME_5 = "APP_NAME_5"
        private const val APP_NAME_6 = "APP_NAME_6"

        private const val APP_PACKAGE_1 = "APP_PACKAGE_1"
        private const val APP_PACKAGE_2 = "APP_PACKAGE_2"
        private const val APP_PACKAGE_3 = "APP_PACKAGE_3"
        private const val APP_PACKAGE_4 = "APP_PACKAGE_4"
        private const val APP_PACKAGE_5 = "APP_PACKAGE_5"
        private const val APP_PACKAGE_6 = "APP_PACKAGE_6"

        private const val APP_USER_HANDLE_1 = "APP_USER_HANDLE_1"
        private const val APP_USER_HANDLE_2 = "APP_USER_HANDLE_2"
        private const val APP_USER_HANDLE_3 = "APP_USER_HANDLE_3"
        private const val APP_USER_HANDLE_4 = "APP_USER_HANDLE_4"
        private const val APP_USER_HANDLE_5 = "APP_USER_HANDLE_5"
        private const val APP_USER_HANDLE_6 = "APP_USER_HANDLE_6"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun getAppName(location: Int): String {
        return when (location) {
            1 -> sharedPreferences.getString(APP_NAME_1, "") ?: ""
            2 -> sharedPreferences.getString(APP_NAME_2, "") ?: ""
            3 -> sharedPreferences.getString(APP_NAME_3, "") ?: ""
            4 -> sharedPreferences.getString(APP_NAME_4, "") ?: ""
            5 -> sharedPreferences.getString(APP_NAME_5, "") ?: ""
            6 -> sharedPreferences.getString(APP_NAME_6, "") ?: ""
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
            else -> ""
        }
    }

    fun setHomeApp(app: MainActivity.AppModel, location: Int) {
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
        }
        editor.apply()
    }
}