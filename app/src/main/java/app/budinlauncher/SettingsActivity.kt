package app.budinlauncher

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import app.budinlauncher.R
import app.budinlauncher.BuildConfig
import java.util.Collections

class SettingsActivity : Activity() {

    companion object {
        private const val FLAG_SET_SWIPE_LEFT_APP = 11
        private const val FLAG_SET_SWIPE_RIGHT_APP = 12
        private const val FLAG_SET_APP_SORTING = 13
        private const val FLAG_SET_HOME_APPS_NUM = 14
    }

    private val appList = mutableListOf<MainActivity.AppModel>()
    private lateinit var appAdapter: AppAdapter
    private lateinit var prefs: Prefs
    private var currentFlag = 0

    override fun attachBaseContext(context: Context) {
        val newConfig = Configuration(context.resources.configuration)
        newConfig.fontScale = Prefs(context).textSizeScale
        applyOverrideConfiguration(newConfig)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme before setting content view
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            Prefs(this).appTheme
        )

        setContentView(R.layout.activity_settings)

        prefs = Prefs(this)
        
        val listView = findViewById<ListView>(R.id.settings_list_view)
        appAdapter = AppAdapter(this, appList)
        listView.adapter = appAdapter

        setupClickListeners()
        refreshAppsList()
        updateSwipeAppsDisplay()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.back_button).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.back_button_list).setOnClickListener {
            hideAppList()
        }

        findViewById<View>(R.id.swipe_left_app).setOnClickListener {
            currentFlag = FLAG_SET_SWIPE_LEFT_APP
            showAppList()
        }

        findViewById<View>(R.id.swipe_right_app).setOnClickListener {
            currentFlag = FLAG_SET_SWIPE_RIGHT_APP
            showAppList()
        }

        findViewById<View>(R.id.screen_time_toggle).setOnClickListener {
            toggleScreenTime()
        }

        findViewById<View>(R.id.app_sorting).setOnClickListener {
            showSortingDialog()
        }

        findViewById<View>(R.id.home_apps_num).setOnClickListener {
            showHomeAppsNumDialog()
        }

        findViewById<View>(R.id.keyboard_auto_show).setOnClickListener {
            toggleKeyboardAutoShow()
        }

        findViewById<View>(R.id.text_size_value).setOnClickListener {
            toggleTextSizeLayout()
        }

        findViewById<View>(R.id.text_size_1).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.ONE)
        }

        findViewById<View>(R.id.text_size_2).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.TWO)
        }

        findViewById<View>(R.id.text_size_3).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.THREE)
        }

        findViewById<View>(R.id.text_size_4).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.FOUR)
        }

        findViewById<View>(R.id.text_size_5).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.FIVE)
        }

        findViewById<View>(R.id.text_size_6).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.SIX)
        }

        findViewById<View>(R.id.text_size_7).setOnClickListener {
            updateTextSizeScale(Constants.TextSize.SEVEN)
        }

        findViewById<View>(R.id.alignment_value).setOnClickListener {
            toggleAlignmentLayout()
        }

        findViewById<View>(R.id.alignment_left).setOnClickListener {
            updateAlignment(Constants.Alignment.LEFT)
        }

        findViewById<View>(R.id.alignment_center).setOnClickListener {
            updateAlignment(Constants.Alignment.CENTER)
        }

        findViewById<View>(R.id.alignment_right).setOnClickListener {
            updateAlignment(Constants.Alignment.RIGHT)
        }

        findViewById<View>(R.id.reset_app_names).setOnClickListener {
            resetCustomAppNames()
        }

        findViewById<View>(R.id.show_app_icons).setOnClickListener {
            toggleShowAppIcons()
        }

        findViewById<View>(R.id.thick_text).setOnClickListener {
            toggleThickText()
        }

        findViewById<View>(R.id.theme_value).setOnClickListener {
            toggleThemeLayout()
        }

        findViewById<View>(R.id.theme_light).setOnClickListener {
            updateTheme(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }

        findViewById<View>(R.id.theme_dark).setOnClickListener {
            updateTheme(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        }

        findViewById<View>(R.id.theme_system).setOnClickListener {
            updateTheme(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun updateSwipeAppsDisplay() {
        findViewById<TextView>(R.id.swipe_left_app).text = prefs.appNameSwipeLeft
        findViewById<TextView>(R.id.swipe_right_app).text = prefs.appNameSwipeRight
        findViewById<TextView>(R.id.home_apps_num).text = prefs.homeAppsNum.toString()
        updateSortingDisplay()
        updateKeyboardDisplay()
        updateScreenTimeDisplay()
        updateTextSizeDisplay()
        updateAlignmentDisplay()
        updateThemeDisplay()
        updateShowAppIconsDisplay()
        updateThickTextDisplay()
        updateSettingsFontFamily()
    }

    private fun updateSortingDisplay() {
        val sortingText = when (prefs.appSortingMode) {
            0 -> "Alphabetical"
            1 -> "Frequent Usage"
            else -> "Alphabetical"
        }
        findViewById<TextView>(R.id.app_sorting).text = sortingText
    }

    private fun updateKeyboardDisplay() {
        val keyboardText = if (prefs.keyboardAutoShow) {
            "On"
        } else {
            "Off"
        }
        findViewById<TextView>(R.id.keyboard_auto_show).text = keyboardText
    }

    private fun updateScreenTimeDisplay() {
        val screenTimeText = if (prefs.screenTimeEnabled) {
            if (ScreenTimeHelper.isUsageAccessPermissionGranted(this)) {
                "On"
            } else {
                "Requires permission"
            }
        } else {
            "Off"
        }
        findViewById<TextView>(R.id.screen_time_toggle).text = screenTimeText
    }

    private fun toggleScreenTime() {
        prefs.screenTimeEnabled = !prefs.screenTimeEnabled
        updateScreenTimeDisplay()
        val message = if (prefs.screenTimeEnabled) {
            if (ScreenTimeHelper.isUsageAccessPermissionGranted(this)) {
                "Screen time enabled"
            } else {
                showUsageAccessDialog()
                "Screen time requires usage access permission"
            }
        } else {
            "Screen time disabled"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showUsageAccessDialog() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun showSortingDialog() {
        val options = arrayOf("Alphabetical", "Frequent Usage")
        val currentSelection = prefs.appSortingMode

        android.app.AlertDialog.Builder(this)
            .setTitle("App Sorting")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                prefs.appSortingMode = which
                updateSortingDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHomeAppsNumDialog() {
        val options = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8")
        val currentSelection = prefs.homeAppsNum

        android.app.AlertDialog.Builder(this)
            .setTitle("Number of Home Apps")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                prefs.homeAppsNum = which
                updateSwipeAppsDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleKeyboardAutoShow() {
        prefs.keyboardAutoShow = !prefs.keyboardAutoShow
        updateKeyboardDisplay()
        val message = if (prefs.keyboardAutoShow) {
            "Keyboard auto-show enabled"
        } else {
            "Keyboard auto-show disabled"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAppList() {
        findViewById<View>(R.id.scrollView).visibility = View.GONE
        findViewById<View>(R.id.app_list_layout).visibility = View.VISIBLE
    }

    private fun hideAppList() {
        findViewById<View>(R.id.scrollView).visibility = View.VISIBLE
        findViewById<View>(R.id.app_list_layout).visibility = View.GONE
    }

    private fun refreshAppsList() {
        Thread {
            try {
                val apps = mutableListOf<MainActivity.AppModel>()
                val userManager = getSystemService(Context.USER_SERVICE) as UserManager
                val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

                for (profile in userManager.userProfiles) {
                    for (activityInfo in launcherApps.getActivityList(null, profile)) {
                        if (!activityInfo.applicationInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                            apps.add(
                                MainActivity.AppModel(
                                    activityInfo.label.toString(),
                                    activityInfo.applicationInfo.packageName,
                                    profile
                                )
                            )
                        }
                    }
                }

                Collections.sort(apps) { app1, app2 ->
                    app1.appLabel.compareTo(app2.appLabel, ignoreCase = true)
                }

                appList.clear()
                appList.addAll(apps)
                
                runOnUiThread {
                    appAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun setSwipeApp(appModel: MainActivity.AppModel) {
        when (currentFlag) {
            FLAG_SET_SWIPE_LEFT_APP -> {
                prefs.appNameSwipeLeft = appModel.appLabel
                prefs.appPackageSwipeLeft = appModel.appPackage
                prefs.appUserHandleSwipeLeft = appModel.userHandle.toString()
            }
            FLAG_SET_SWIPE_RIGHT_APP -> {
                prefs.appNameSwipeRight = appModel.appLabel
                prefs.appPackageSwipeRight = appModel.appPackage
                prefs.appUserHandleSwipeRight = appModel.userHandle.toString()
            }
        }
        updateSwipeAppsDisplay()
        hideAppList()
    }

    class AppAdapter(
        private val context: Context,
        private var filteredAppsList: List<MainActivity.AppModel>
    ) : BaseAdapter(), Filterable {

        private val allAppsList: List<MainActivity.AppModel> = filteredAppsList.toList()

        private class ViewHolder {
            lateinit var appName: TextView
        }

        override fun getCount(): Int {
            return filteredAppsList.size
        }

        override fun getItem(position: Int): Any {
            return filteredAppsList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val appModel = getItem(position) as MainActivity.AppModel
            val viewHolder: ViewHolder
            val view: View

            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                view = inflater.inflate(R.layout.adapter_app, parent, false)
                viewHolder.appName = view.findViewById(R.id.app_name)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.appName.text = appModel.appLabel

            // Set font family based on thick text preference
            val prefs = Prefs(context)
            viewHolder.appName.typeface = android.graphics.Typeface.create(prefs.getFontFamily(), android.graphics.Typeface.NORMAL)

            viewHolder.appName.setOnClickListener {
                val activity = context as SettingsActivity
                activity.setSwipeApp(appModel)
            }

            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    @Suppress("UNCHECKED_CAST")
                    filteredAppsList = results.values as List<MainActivity.AppModel>
                    notifyDataSetChanged()
                }

                override fun performFiltering(constraint: CharSequence): FilterResults {
                    val results = FilterResults()
                    val filteredApps = mutableListOf<MainActivity.AppModel>()

                    if (constraint.isEmpty()) {
                        filteredApps.addAll(allAppsList)
                    } else {
                        val lowerCaseConstraint = constraint.toString().lowercase()
                        for (app in allAppsList) {
                            if (app.appLabel.lowercase().contains(lowerCaseConstraint)) {
                                filteredApps.add(app)
                            }
                        }
                    }

                    results.count = filteredApps.size
                    results.values = filteredApps
                    return results
                }
            }
        }
    }

    private fun updateTextSizeDisplay() {
        val textSizeValue = when (prefs.textSizeScale) {
            Constants.TextSize.ONE -> 1
            Constants.TextSize.TWO -> 2
            Constants.TextSize.THREE -> 3
            Constants.TextSize.FOUR -> 4
            Constants.TextSize.FIVE -> 5
            Constants.TextSize.SIX -> 6
            Constants.TextSize.SEVEN -> 7
            else -> 4
        }
        findViewById<TextView>(R.id.text_size_value).text = textSizeValue.toString()
    }

    private fun toggleTextSizeLayout() {
        val textSizesLayout = findViewById<View>(R.id.text_sizes_layout)
        if (textSizesLayout.visibility == View.VISIBLE) {
            textSizesLayout.visibility = View.GONE
        } else {
            textSizesLayout.visibility = View.VISIBLE
        }
    }

    private fun updateTextSizeScale(sizeScale: Float) {
        if (prefs.textSizeScale == sizeScale) return
        prefs.textSizeScale = sizeScale
        updateTextSizeDisplay()
        findViewById<View>(R.id.text_sizes_layout).visibility = View.GONE

        // Restart the main activity to apply the text size change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun toggleShowAppIcons() {
        prefs.showAppIcons = !prefs.showAppIcons
        updateShowAppIconsDisplay()

        // Restart the main activity to apply the change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun resetCustomAppNames() {
        // Get all installed apps and remove their custom names
        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager

        for (profile in userManager.userProfiles) {
            for (activityInfo in launcherApps.getActivityList(null, profile)) {
                if (!activityInfo.applicationInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                    prefs.removeCustomAppName(activityInfo.applicationInfo.packageName)
                }
            }
        }

        Toast.makeText(this, "Custom app names reset", Toast.LENGTH_SHORT).show()
    }

    private fun updateAlignmentDisplay() {
        val alignmentText = when (prefs.homeAlignment) {
            Constants.Alignment.LEFT -> "Left"
            Constants.Alignment.CENTER -> "Center"
            Constants.Alignment.RIGHT -> "Right"
            else -> "Left"
        }
        findViewById<TextView>(R.id.alignment_value).text = alignmentText
    }

    private fun toggleAlignmentLayout() {
        val alignmentLayout = findViewById<View>(R.id.alignment_layout)
        if (alignmentLayout.visibility == View.VISIBLE) {
            alignmentLayout.visibility = View.GONE
        } else {
            alignmentLayout.visibility = View.VISIBLE
        }
    }

    private fun updateAlignment(alignment: Int) {
        if (prefs.homeAlignment == alignment) return
        prefs.homeAlignment = alignment
        updateAlignmentDisplay()
        findViewById<View>(R.id.alignment_layout).visibility = View.GONE

        // Restart the main activity to apply the alignment change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun updateThemeDisplay() {
        val themeText = when (prefs.appTheme) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System"
            else -> "Dark"
        }
        findViewById<TextView>(R.id.theme_value).text = themeText
    }

    private fun updateShowAppIconsDisplay() {
        val showAppIconsText = if (prefs.showAppIcons) "On" else "Off"
        findViewById<TextView>(R.id.show_app_icons).text = showAppIconsText
    }

    private fun toggleThickText() {
        prefs.thickText = !prefs.thickText
        updateThickTextDisplay()
    }

    private fun updateThickTextDisplay() {
        val thickTextText = if (prefs.thickText) "On" else "Off"
        findViewById<TextView>(R.id.thick_text).text = thickTextText
    }

    private fun updateSettingsFontFamily() {
        // Update font family for settings UI elements
        val fontFamily = prefs.getFontFamily()
        val typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)

        // Update main text views
        findViewById<TextView>(R.id.back_button).typeface = typeface

        // Update section headers and labels
        val rootView = findViewById<View>(android.R.id.content)
        updateTextViewFontFamily(rootView, typeface)
    }

    private fun updateTextViewFontFamily(view: View, typeface: android.graphics.Typeface) {
        if (view is TextView) {
            view.typeface = typeface
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateTextViewFontFamily(view.getChildAt(i), typeface)
            }
        }
    }

    private fun toggleThemeLayout() {
        val themeLayout = findViewById<View>(R.id.theme_layout)
        if (themeLayout.visibility == View.VISIBLE) {
            themeLayout.visibility = View.GONE
        } else {
            themeLayout.visibility = View.VISIBLE
        }
    }

    private fun updateTheme(themeMode: Int) {
        if (prefs.appTheme == themeMode) return
        prefs.appTheme = themeMode
        updateThemeDisplay()
        findViewById<View>(R.id.theme_layout).visibility = View.GONE

        // Apply the theme immediately
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(themeMode)

        // Restart activities to apply the theme change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}