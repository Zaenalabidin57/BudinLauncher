package app.budinlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import android.view.ViewTreeObserver
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.Shader
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.reflect.Method
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

class MainActivity : Activity(), View.OnClickListener, View.OnLongClickListener {

    companion object {
        private const val FLAG_LAUNCH_APP = 0
    }

    private val appList = mutableListOf<AppModel>()

    private lateinit var prefs: Prefs
    private lateinit var launcherApps: LauncherApps
    private lateinit var appDrawer: View
    private lateinit var appDrawerBackground: View
    private lateinit var search: EditText
    private lateinit var appListView: ListView
    private lateinit var appAdapter: AppAdapter
    private lateinit var homeAppsLayout: LinearLayout
    private lateinit var homeApp1: TextView
    private lateinit var homeApp2: TextView
    private lateinit var homeApp3: TextView
    private lateinit var homeApp4: TextView
    private lateinit var homeApp5: TextView
    private lateinit var homeApp6: TextView
    private lateinit var homeApp7: TextView
    private lateinit var homeApp8: TextView
    private lateinit var setDefaultLauncher: TextView

    interface AppClickListener {
        fun appClicked(appModel: AppModel, flag: Int)
        fun appLongPress(appModel: AppModel)
    }

    override fun onBackPressed() {
        if (appDrawer.visibility == View.VISIBLE) {
            backToHome()
        }
    }

    override fun attachBaseContext(context: Context) {
        val newConfig = Configuration(context.resources.configuration)
        newConfig.fontScale = Prefs(context).textSizeScale
        applyOverrideConfiguration(newConfig)

        // Apply theme mode early
        AppCompatDelegate.setDefaultNightMode(Prefs(context).appTheme)

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize prefs before setting theme
        prefs = Prefs(this)

        // Set theme before setting content view
        AppCompatDelegate.setDefaultNightMode(prefs.appTheme)

        setContentView(R.layout.activity_main)
        
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        findViewById<View>(R.id.layout_main).setOnTouchListener(getSwipeGestureListener(this))

        // Initialize views before calling methods that use them
        launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        search = findViewById(R.id.search)
        homeAppsLayout = findViewById(R.id.home_apps_layout)
        appDrawer = findViewById(R.id.app_drawer_layout)
        appDrawerBackground = findViewById(R.id.app_drawer_background)

        initClickListeners()
        setHomeAlignment()

        appAdapter = AppAdapter(this, appList, getAppClickListener())
        appListView = findViewById(R.id.app_list_view)
        appListView.adapter = appAdapter

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                appAdapter.filter.filter(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        
        appListView.setOnScrollListener(getScrollListener())
    }

    override fun onResume() {
        super.onResume()
        backToHome()
        populateHomeApps()

        // Update text sizes and alignment dynamically
        updateHomeTextSizes()
        updateHomeAppIcons()
        updateHomeFontFamily()
        setHomeAlignment()

        // Recreate adapter with updated context to ensure proper text scaling
        if (::appAdapter.isInitialized) {
            appAdapter = AppAdapter(this, appList, getAppClickListener())
            appListView.adapter = appAdapter
        }

        refreshAppsList()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.set_as_default_launcher -> resetDefaultLauncher()
            //R.id.clock -> startActivity(Intent(Intent(AlarmClock.ACTION_SHOW_ALARMS)))
            R.id.clock -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.date -> {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
                intent.setPackage(null)
                startActivity(intent)
            }
            else -> {
                try {
                    val location = view.tag.toString().toInt()
                    homeAppClicked(location)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onLongClick(view: View): Boolean {
        try {
            val location = view.tag.toString().toInt()
            showAppList(location)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun initClickListeners() {
        setDefaultLauncher = findViewById(R.id.set_as_default_launcher)
        setDefaultLauncher.setOnClickListener(this)

        findViewById<View>(R.id.clock).setOnClickListener(this)
        findViewById<View>(R.id.date).setOnClickListener(this)

        homeApp1 = findViewById(R.id.home_app_1)
        homeApp2 = findViewById(R.id.home_app_2)
        homeApp3 = findViewById(R.id.home_app_3)
        homeApp4 = findViewById(R.id.home_app_4)
        homeApp5 = findViewById(R.id.home_app_5)
        homeApp6 = findViewById(R.id.home_app_6)
        homeApp7 = findViewById(R.id.home_app_7)
        homeApp8 = findViewById(R.id.home_app_8)

        homeApp1.setOnClickListener(this)
        homeApp2.setOnClickListener(this)
        homeApp3.setOnClickListener(this)
        homeApp4.setOnClickListener(this)
        homeApp5.setOnClickListener(this)
        homeApp6.setOnClickListener(this)
        homeApp7.setOnClickListener(this)
        homeApp8.setOnClickListener(this)

        homeApp1.setOnLongClickListener(this)
        homeApp2.setOnLongClickListener(this)
        homeApp3.setOnLongClickListener(this)
        homeApp4.setOnLongClickListener(this)
        homeApp5.setOnLongClickListener(this)
        homeApp6.setOnLongClickListener(this)
        homeApp7.setOnLongClickListener(this)
        homeApp8.setOnLongClickListener(this)
    }

    private fun populateHomeApps() {
        val homeAppsNum = prefs.homeAppsNum

        // Hide all home apps first
        homeApp1.visibility = View.GONE
        homeApp2.visibility = View.GONE
        homeApp3.visibility = View.GONE
        homeApp4.visibility = View.GONE
        homeApp5.visibility = View.GONE
        homeApp6.visibility = View.GONE
        homeApp7.visibility = View.GONE
        homeApp8.visibility = View.GONE

        if (homeAppsNum == 0) return

        // Show and populate apps based on the number
        homeApp1.visibility = View.VISIBLE
        homeApp1.text = prefs.getAppName(1)
        if (homeAppsNum == 1) return

        homeApp2.visibility = View.VISIBLE
        homeApp2.text = prefs.getAppName(2)
        if (homeAppsNum == 2) return

        homeApp3.visibility = View.VISIBLE
        homeApp3.text = prefs.getAppName(3)
        if (homeAppsNum == 3) return

        homeApp4.visibility = View.VISIBLE
        homeApp4.text = prefs.getAppName(4)
        if (homeAppsNum == 4) return

        homeApp5.visibility = View.VISIBLE
        homeApp5.text = prefs.getAppName(5)
        if (homeAppsNum == 5) return

        homeApp6.visibility = View.VISIBLE
        homeApp6.text = prefs.getAppName(6)
        if (homeAppsNum == 6) return

        homeApp7.visibility = View.VISIBLE
        homeApp7.text = prefs.getAppName(7)
        if (homeAppsNum == 7) return

        homeApp8.visibility = View.VISIBLE
        homeApp8.text = prefs.getAppName(8)
    }

    private fun showLongPressToast() {
        Toast.makeText(this, "Long press to select app", Toast.LENGTH_SHORT).show()
    }

    private fun backToHome() {
        appDrawer.visibility = View.GONE
        homeAppsLayout.visibility = View.VISIBLE
        appAdapter.setFlag(FLAG_LAUNCH_APP)
        hideKeyboard()
        appListView.setSelectionAfterHeaderView()
        checkForDefaultLauncher()

        // Remove blur effect when returning to home
        removeBlurFromBackground()
    }

    private fun refreshAppsList() {
        Thread {
            try {
                val apps = mutableListOf<AppModel>()
                val userManager = getSystemService(Context.USER_SERVICE) as UserManager

                for (profile in userManager.userProfiles) {
                    for (activityInfo in launcherApps.getActivityList(null, profile)) {
                        if (!activityInfo.applicationInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                            apps.add(
                                AppModel(
                                    activityInfo.label.toString(),
                                    activityInfo.applicationInfo.packageName,
                                    profile
                                )
                            )
                        }
                    }
                }
                
                Collections.sort(apps) { app1, app2 ->
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
                
                appList.clear()
                appList.addAll(apps)
                appAdapter.updateAppList(appList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAppList(flag: Int) {
        setDefaultLauncher.visibility = View.GONE
        search.text.clear()
        appAdapter.setFlag(flag)
        homeAppsLayout.visibility = View.GONE
        appDrawer.visibility = View.VISIBLE

        // Apply blur effect to app drawer background
        applyBlurToBackground()

        // Show keyboard with a delay to ensure the layout is fully rendered
        if (prefs.keyboardAutoShow) {
            appDrawer.postDelayed({
                showKeyboard()
            }, 100) // Small delay to ensure the app drawer is fully visible
        }
    }

    private fun showKeyboard() {
        search.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        search.clearFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(search.windowToken, 0)
    }

    private fun applyBlurToBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use RenderEffect for Android 12+ (API 31+)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                        25f, 25f, // blur radius X and Y
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    appDrawerBackground.setRenderEffect(blurEffect)
                    appDrawerBackground.setBackgroundColor(Color.argb(100, 255, 255, 255)) // White semi-transparent layer
                }
            } catch (e: Exception) {
                // Fallback if RenderEffect fails
                applyFallbackBlur()
            }
        } else {
            // Remove any existing blur effect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appDrawerBackground.setRenderEffect(null)
            }
            applyFallbackBlur()
        }
    }

    private fun applyFallbackBlur() {
        // Always apply blur for better user experience
        appDrawerBackground.setBackgroundColor(Color.argb(180, 0, 0, 0)) // Dark semi-transparent layer
    }

    private fun removeBlurFromBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appDrawerBackground.setRenderEffect(null)
        }
        appDrawerBackground.setBackgroundColor(Color.TRANSPARENT)
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    private fun expandNotificationDrawer() {
        try {
            val statusBarService = getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareToLaunchApp(appModel: AppModel) {
        hideKeyboard()
        prefs.incrementAppUsage(appModel.appPackage)
        launchApp(appModel)
        backToHome()
        search.text.clear()
    }

    private fun homeAppClicked(location: Int) {
        if (prefs.getAppPackage(location).isEmpty()) {
            showLongPressToast()
        } else {
            val appPackage = prefs.getAppPackage(location)
            prefs.incrementAppUsage(appPackage)
            launchApp(
                getAppModel(
                    prefs.getAppName(location),
                    appPackage,
                    prefs.getAppUserHandle(location)
                )
            )
        }
    }

    private fun launchApp(appModel: AppModel) {
        val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val appLaunchActivityList = launcher.getActivityList(appModel.appPackage, appModel.userHandle)
        val componentName: ComponentName

        when (appLaunchActivityList.size) {
            0 -> {
                Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
                return
            }
            1 -> {
                componentName = ComponentName(
                    appModel.appPackage,
                    appLaunchActivityList[0].name
                )
            }
            else -> {
                componentName = ComponentName(
                    appModel.appPackage,
                    appLaunchActivityList[appLaunchActivityList.size - 1].name
                )
            }
        }

        try {
            launcher.startMainActivity(componentName, appModel.userHandle, null, null)
        } catch (securityException: SecurityException) {
            launcher.startMainActivity(componentName, android.os.Process.myUserHandle(), null, null)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo(appModel: AppModel) {
        val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val intent = packageManager.getLaunchIntentForPackage(appModel.appPackage)
        if (intent == null || intent.component == null) return
        launcher.startAppDetailsActivity(intent.component!!, appModel.userHandle, null, null)
    }

    private fun setHomeApp(appModel: AppModel, flag: Int) {
        prefs.setHomeApp(appModel, flag)
        backToHome()
        populateHomeApps()
    }

    private fun checkForDefaultLauncher() {
        if (BuildConfig.APPLICATION_ID == getDefaultLauncherPackage()) {
            setDefaultLauncher.visibility = View.GONE
        } else {
            setDefaultLauncher.visibility = View.VISIBLE
        }
    }

    private fun getDefaultLauncherPackage(): String {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        val result = packageManager.resolveActivity(intent, 0)
        return if (result == null || result.activityInfo == null) {
            "android"
        } else {
            result.activityInfo.packageName
        }
    }

    private fun resetDefaultLauncher() {
        try {
            val packageManager = packageManager
            val componentName = ComponentName(this, FakeHomeActivity::class.java)
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.setPackage(null)
            startActivity(intent)
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (getDefaultLauncherPackage().contains(".")) {
            openLauncherPhoneSettings()
        }
    }

    private fun openLauncherPhoneSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Toast.makeText(this, "Set BudinLauncher as default launcher", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } else {
            Toast.makeText(this, "Search for launcher or home apps", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun openEditSettingsPermission() {
        Toast.makeText(this, "Please grant this permission", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun getAppClickListener(): AppClickListener {
        return object : AppClickListener {
            override fun appClicked(appModel: AppModel, flag: Int) {
                if (flag == FLAG_LAUNCH_APP) {
                    prepareToLaunchApp(appModel)
                } else {
                    setHomeApp(appModel, flag)
                }
            }

            override fun appLongPress(appModel: AppModel) {
                hideKeyboard()
                openAppInfo(appModel)
            }
        }
    }

    private fun getAppModel(appLabel: String, appPackage: String, appUserHandle: String): AppModel {
        return AppModel(appLabel, appPackage, getUserHandleFromString(appUserHandle))
    }

    private fun getUserHandleFromString(appUserHandleString: String): UserHandle {
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager
        for (userHandle in userManager.userProfiles) {
            if (userHandle.toString() == appUserHandleString) {
                return userHandle
            }
        }
        return android.os.Process.myUserHandle()
    }

    private fun getScrollListener(): AbsListView.OnScrollListener {
        return object : AbsListView.OnScrollListener {
            private var onTop = false

            override fun onScrollStateChanged(listView: AbsListView, state: Int) {
                if (state == 1) { // dragging
                    onTop = !listView.canScrollVertically(-1)
                    if (onTop) hideKeyboard()
                } else if (state == 0) { // stopped
                    if (!listView.canScrollVertically(1)) {
                        hideKeyboard()
                    } else if (!listView.canScrollVertically(-1)) {
                        if (onTop) {
                            backToHome()
                        } else {
                            showKeyboard()
                        }
                    }
                }
            }

            override fun onScroll(absListView: AbsListView, i: Int, i1: Int, i2: Int) {}
        }
    }

    private fun launchSwipeApp(packageName: String, userHandleString: String) {
        if (packageName.isEmpty()) {
            Toast.makeText(this, "No app set for swipe gesture", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val userHandle = getUserHandleFromString(userHandleString)
            val appLaunchActivityList = launcherApps.getActivityList(packageName, userHandle)

            val componentName: ComponentName
            when (appLaunchActivityList.size) {
                0 -> {
                    Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
                    return
                }
                1 -> {
                    componentName = ComponentName(
                        packageName,
                        appLaunchActivityList[0].name
                    )
                }
                else -> {
                    componentName = ComponentName(
                        packageName,
                        appLaunchActivityList[appLaunchActivityList.size - 1].name
                    )
                }
            }

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.component = componentName

            if (userHandle != android.os.Process.myUserHandle()) {
                launcherApps.startMainActivity(componentName, userHandle, null, null)
            } else {
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSwipeGestureListener(context: Context): OnSwipeTouchListener {
        return object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                launchSwipeApp(prefs.appPackageSwipeLeft, prefs.appUserHandleSwipeLeft)
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                launchSwipeApp(prefs.appPackageSwipeRight, prefs.appUserHandleSwipeRight)
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                showAppList(FLAG_LAUNCH_APP)
            }

            override fun onSwipeDown() {
                super.onSwipeDown()
                expandNotificationDrawer()
            }

            override fun onLongClick() {
                super.onLongClick()
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
    }

    data class AppModel(
        val appLabel: String,
        val appPackage: String,
        val userHandle: UserHandle
    )

    class AppAdapter(
        private val context: Context,
        private var filteredAppsList: List<AppModel>,
        private val appClickListener: AppClickListener
    ) : BaseAdapter(), Filterable {

        private var allAppsList: List<AppModel> = filteredAppsList.toList()
        private var flag = 0

        fun updateAppList(newAppList: List<AppModel>) {
            @Suppress("UNCHECKED_CAST")
            filteredAppsList = newAppList
            allAppsList = newAppList.toList()
            notifyDataSetChanged()
        }

        private class ViewHolder {
            lateinit var appName: TextView
            lateinit var indicator: View
            lateinit var appMenuLayout: LinearLayout
            lateinit var appUninstall: TextView
            lateinit var appRename: TextView
            lateinit var appInfo: TextView
            lateinit var appClose: TextView
            lateinit var renameLayout: LinearLayout
            lateinit var renameEditText: EditText
            lateinit var saveRename: TextView
            lateinit var closeRename: TextView
        }

        fun setFlag(flag: Int) {
            this.flag = flag
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
            val appModel = getItem(position) as AppModel
            val viewHolder: ViewHolder
            val view: View

            if (convertView == null) {
                viewHolder = ViewHolder()
                val inflater = LayoutInflater.from(context)
                view = inflater.inflate(R.layout.adapter_app_menu, parent, false)
                viewHolder.appName = view.findViewById(R.id.app_name)
                viewHolder.indicator = view.findViewById(R.id.other_profile_indicator)
                viewHolder.appMenuLayout = view.findViewById(R.id.app_menu_layout)
                viewHolder.appUninstall = view.findViewById(R.id.app_uninstall)
                viewHolder.appRename = view.findViewById(R.id.app_rename)
                viewHolder.appInfo = view.findViewById(R.id.app_info)
                viewHolder.appClose = view.findViewById(R.id.app_close)
                viewHolder.renameLayout = view.findViewById(R.id.rename_layout)
                viewHolder.renameEditText = view.findViewById(R.id.rename_edit_text)
                viewHolder.saveRename = view.findViewById(R.id.save_rename)
                viewHolder.closeRename = view.findViewById(R.id.close_rename)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.appName.tag = appModel
            viewHolder.appName.text = getCustomAppName(context, appModel.appPackage, appModel.appLabel)

            // Set font family based on thick text preference
            val prefs = Prefs(context)
            viewHolder.appName.typeface = android.graphics.Typeface.create(prefs.getFontFamily(), android.graphics.Typeface.NORMAL)

            // Set app icon if enabled
            if (prefs.showAppIcons) {
                try {
                    val icon = context.packageManager.getApplicationIcon(appModel.appPackage)
                    // Set bounds to make icon consistent size
                    val iconSize = (48 * context.resources.displayMetrics.density).toInt()
                    icon.setBounds(0, 0, iconSize, iconSize)
                    viewHolder.appName.setCompoundDrawables(icon, null, null, null)
                } catch (e: Exception) {
                    // Fallback to default icon if app icon not available
                    val defaultIcon = context.getDrawable(android.R.drawable.sym_def_app_icon)
                    val iconSize = (48 * context.resources.displayMetrics.density).toInt()
                    defaultIcon?.setBounds(0, 0, iconSize, iconSize)
                    viewHolder.appName.setCompoundDrawables(defaultIcon, null, null, null)
                }
            } else {
                viewHolder.appName.setCompoundDrawables(null, null, null, null)
            }

            viewHolder.appName.setOnClickListener { view ->
                val clickedAppModel = view.tag as AppModel
                appClickListener.appClicked(clickedAppModel, flag)
            }
            viewHolder.appName.setOnLongClickListener { view ->
                val clickedAppModel = view.tag as AppModel
                showAppMenu(viewHolder, clickedAppModel)
                true
            }

            // Set up menu click listeners
            viewHolder.appUninstall.setOnClickListener {
                val clickedAppModel = viewHolder.appName.tag as AppModel
                uninstallApp(clickedAppModel)
                hideAppMenu(viewHolder)
            }

            viewHolder.appRename.setOnClickListener {
                val clickedAppModel = viewHolder.appName.tag as AppModel
                showRenameDialog(viewHolder, clickedAppModel)
            }

            viewHolder.appInfo.setOnClickListener {
                val clickedAppModel = viewHolder.appName.tag as AppModel
                appClickListener.appLongPress(clickedAppModel)
                hideAppMenu(viewHolder)
            }

            viewHolder.appClose.setOnClickListener {
                hideAppMenu(viewHolder)
            }

            viewHolder.saveRename.setOnClickListener {
                val clickedAppModel = viewHolder.appName.tag as AppModel
                saveAppName(viewHolder, clickedAppModel)
            }

            viewHolder.closeRename.setOnClickListener {
                hideRenameDialog(viewHolder)
            }

            if (appModel.userHandle == android.os.Process.myUserHandle()) {
                viewHolder.indicator.visibility = View.GONE
            } else {
                viewHolder.indicator.visibility = View.VISIBLE
            }

            if (flag == 0 && count == 1) {
                appClickListener.appClicked(appModel, flag)
            }

            return view
        }

        private fun showAppMenu(viewHolder: ViewHolder, appModel: AppModel) {
            viewHolder.appName.visibility = View.INVISIBLE
            viewHolder.appMenuLayout.visibility = View.VISIBLE

            // Disable uninstall for system apps
            val isSystemApp = isSystemApp(context, appModel.appPackage)
            viewHolder.appUninstall.alpha = if (isSystemApp) 0.5f else 1.0f
            viewHolder.appUninstall.isEnabled = !isSystemApp
        }

        private fun hideAppMenu(viewHolder: ViewHolder) {
            viewHolder.appMenuLayout.visibility = View.GONE
            viewHolder.appName.visibility = View.VISIBLE
        }

        private fun showRenameDialog(viewHolder: ViewHolder, appModel: AppModel) {
            viewHolder.renameEditText.setText(getCustomAppName(context, appModel.appPackage, appModel.appLabel))
            viewHolder.renameEditText.hint = appModel.appLabel
            viewHolder.renameEditText.setSelectAllOnFocus(true)
            viewHolder.renameEditText.requestFocus()
            viewHolder.renameLayout.visibility = View.VISIBLE
            viewHolder.appMenuLayout.visibility = View.GONE

            // Show keyboard
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(viewHolder.renameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        private fun hideRenameDialog(viewHolder: ViewHolder) {
            viewHolder.renameLayout.visibility = View.GONE
            viewHolder.appName.visibility = View.VISIBLE

            // Hide keyboard
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(viewHolder.renameEditText.windowToken, 0)
        }

        private fun saveAppName(viewHolder: ViewHolder, appModel: AppModel) {
            val newName = viewHolder.renameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                setCustomAppName(context, appModel.appPackage, newName)
                viewHolder.appName.text = newName
            }
            hideRenameDialog(viewHolder)
        }

        private fun uninstallApp(appModel: AppModel) {
            try {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${appModel.appPackage}")
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Unable to uninstall app", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getCustomAppName(context: Context, packageName: String, defaultName: String): String {
            val prefs = Prefs(context)
            return prefs.getCustomAppName(packageName, defaultName)
        }

        private fun setCustomAppName(context: Context, packageName: String, name: String) {
            val prefs = Prefs(context)
            prefs.setCustomAppName(packageName, name)
        }

        private fun isSystemApp(context: Context, packageName: String): Boolean {
            return try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    @Suppress("UNCHECKED_CAST")
                    filteredAppsList = results.values as List<AppModel>
                    notifyDataSetChanged()
                }

                override fun performFiltering(constraint: CharSequence): FilterResults {
                    val results = FilterResults()
                    val filteredApps = mutableListOf<AppModel>()

                    if (constraint.isBlank()) {
                        filteredApps.addAll(allAppsList)
                    } else {
                        val lowerCaseConstraint = constraint.toString().lowercase().trim()
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

    private fun setHomeAlignment() {
        val horizontalGravity = prefs.homeAlignment
        val verticalGravity = if (prefs.homeBottomAlignment) Gravity.BOTTOM else Gravity.CENTER_VERTICAL

        homeAppsLayout.gravity = horizontalGravity or verticalGravity

        // Apply alignment to individual app text views
        homeApp1.gravity = horizontalGravity
        homeApp2.gravity = horizontalGravity
        homeApp3.gravity = horizontalGravity
        homeApp4.gravity = horizontalGravity
        homeApp5.gravity = horizontalGravity
        homeApp6.gravity = horizontalGravity
        homeApp7.gravity = horizontalGravity
        homeApp8.gravity = horizontalGravity
    }

    private fun updateHomeTextSizes() {
        // Update text sizes for home apps based on current font scale
        val scaledTextSize = resources.getDimension(R.dimen.text_large) * prefs.textSizeScale

        homeApp1.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp2.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp3.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp4.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp5.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp6.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp7.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        homeApp8.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)

        // Update clock and date text sizes
        val scaledClockSize = resources.getDimension(R.dimen.text_clock) * prefs.textSizeScale
        findViewById<TextView>(R.id.clock).setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledClockSize)
        findViewById<TextView>(R.id.date).setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledClockSize * 0.4f) // Date is smaller
    }

    private fun updateHomeFontFamily() {
        // Update font family for home apps based on thick text preference
        val fontFamily = prefs.getFontFamily()

        homeApp1.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp2.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp3.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp4.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp5.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp6.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp7.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        homeApp8.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)

        // Update clock and date font family
        findViewById<TextView>(R.id.clock).typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        findViewById<TextView>(R.id.date).typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)

        // Update search field font family
        search.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
    }

    private fun updateHomeAppIcons() {
        // Always hide icons on home screen - keep minimal look
        homeApp1.setCompoundDrawables(null, null, null, null)
        homeApp2.setCompoundDrawables(null, null, null, null)
        homeApp3.setCompoundDrawables(null, null, null, null)
        homeApp4.setCompoundDrawables(null, null, null, null)
        homeApp5.setCompoundDrawables(null, null, null, null)
        homeApp6.setCompoundDrawables(null, null, null, null)
        homeApp7.setCompoundDrawables(null, null, null, null)
        homeApp8.setCompoundDrawables(null, null, null, null)
    }
}
