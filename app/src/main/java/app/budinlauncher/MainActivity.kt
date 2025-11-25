package app.budinlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.budinlauncher.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Collections
import android.os.VibrationEffect
import android.os.Vibrator

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    companion object {
        private const val FLAG_LAUNCH_APP = 0
    }

    private val appList = mutableListOf<AppModel>()

    private lateinit var prefs: Prefs
    private lateinit var launcherApps: LauncherApps
    private lateinit var userManager: UserManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var appAdapter: AppAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var vibrator: Vibrator

    // Battery monitoring
    private lateinit var batteryReceiver: BroadcastReceiver

    interface AppClickListener {
        fun appClicked(appModel: AppModel, flag: Int)
        fun appLongPress(appModel: AppModel)
    }

    private fun performHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }



    override fun attachBaseContext(context: Context) {
        val newConfig = Configuration(context.resources.configuration)
        newConfig.fontScale = Prefs(context).textSizeScale
        applyOverrideConfiguration(newConfig)

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize prefs before setting theme
        prefs = Prefs(this)

        // Set theme before setting content view
        AppCompatDelegate.setDefaultNightMode(prefs.appTheme)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Make the activity fullscreen and hide system UI
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        // Hide system UI for immersive experience
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        
        // Initialize battery monitoring
        initBatteryMonitoring()

        binding.layoutMain.setOnTouchListener(getSwipeGestureListener(this))

        // Initialize views before calling methods that use them
        launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        userManager = getSystemService(Context.USER_SERVICE) as UserManager
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        initClickListeners()
        setupNavigationStyle()
        setupPlanetLauncher()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.appDrawerLayout.visibility == View.VISIBLE) {
                    backToHome()
                }
            }
        })

        // Setup app drawer for app list functionality
        setupAppDrawer()
    }

    private fun setupNavigationStyle() {
        when (prefs.navigationStyle) {
            Prefs.Constants.NavigationStyle.LIST -> {
                binding.traditionalAppListContainer.visibility = View.VISIBLE
                binding.planetLauncherView.visibility = View.GONE
                setupTraditionalAppList()
                setHomeAlignment() // Apply alignment when switching to list mode
            }
            Prefs.Constants.NavigationStyle.PLANET -> {
                binding.traditionalAppListContainer.visibility = View.GONE
                binding.planetLauncherView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTraditionalAppList() {
        // Set up click listeners for traditional home apps
        binding.homeApp1.setOnClickListener(this)
        binding.homeApp2.setOnClickListener(this)
        binding.homeApp3.setOnClickListener(this)
        binding.homeApp4.setOnClickListener(this)
        binding.homeApp5.setOnClickListener(this)
        binding.homeApp6.setOnClickListener(this)
        binding.homeApp7.setOnClickListener(this)
        binding.homeApp8.setOnClickListener(this)

        binding.homeApp1.setOnLongClickListener(this)
        binding.homeApp2.setOnLongClickListener(this)
        binding.homeApp3.setOnLongClickListener(this)
        binding.homeApp4.setOnLongClickListener(this)
        binding.homeApp5.setOnLongClickListener(this)
        binding.homeApp6.setOnLongClickListener(this)
        binding.homeApp7.setOnLongClickListener(this)
        binding.homeApp8.setOnLongClickListener(this)

        populateHomeApps()
    }

    private fun setupPlanetLauncher() {
        binding.planetLauncherView.setOnAppClickListener { appModel ->
            prepareToLaunchApp(appModel)
        }

        lifecycleScope.launch {
            // Use home apps (added apps) instead of most frequent apps
            val homeApps = getHomeApps()
            binding.planetLauncherView.setApps(homeApps)
        }
    }

    private fun getHomeApps(): List<AppModel> {
        val homeApps = mutableListOf<AppModel>()

        // Get all installed apps to match against home app packages
        val allApps = mutableListOf<AppModel>()
        for (profile in userManager.userProfiles) {
            for (activityInfo in launcherApps.getActivityList(null, profile)) {
                if (activityInfo.applicationInfo.packageName != packageName) {
                    allApps.add(
                        AppModel(
                            activityInfo.label.toString(),
                            activityInfo.applicationInfo.packageName,
                            profile
                        )
                    )
                }
            }
        }

        // Get home app packages from preferences (1-8)
        for (i in 1..8) {
            val package_name = prefs.getAppPackage(i)
            val app_name = prefs.getAppName(i)
            val userHandleStr = prefs.getAppUserHandle(i)

            if (package_name.isNotEmpty()) {
                // Find matching app in all apps
                val matchingApp = allApps.find {
                    it.appPackage == package_name
                }
                if (matchingApp != null) {
                    homeApps.add(matchingApp)
                }
            }
        }

        return homeApps
    }

    private fun setupAppDrawer() {
        appAdapter = AppAdapter(this, appList, getAppClickListener())
        binding.appListView.layoutManager = LinearLayoutManager(this)
        binding.appListView.adapter = appAdapter

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                appAdapter.filter.filter(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.appListView.addOnScrollListener(getScrollListener())

        lifecycleScope.launch {
            viewModel.appList.collect { apps ->
                appList.clear()
                appList.addAll(apps)
                appAdapter.updateAppList(appList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        backToHome()

        // Update navigation style in case it changed
        setupNavigationStyle()

        // Update text sizes dynamically
        updateHomeTextSizes()
        updateHomeFontFamily()

        // Update traditional app list if needed
        if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
            populateHomeApps()
            setHomeAlignment()
        } else {
            // Ensure planet launcher is properly set up
            setupPlanetLauncher()
        }

        refreshAppsList()

        // Update battery status
        updateBatteryStatus()

        // Update screen time display
        updateScreenTimeDisplay()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister battery receiver
        if (::batteryReceiver.isInitialized) {
            unregisterReceiver(batteryReceiver)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.set_as_default_launcher -> resetDefaultLauncher()
            R.id.clock -> {
                performHapticFeedback()
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.date -> {
                performHapticFeedback()
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
                intent.setPackage(null)
                startActivity(intent)
            }
            else -> {
                // Handle traditional home app clicks
                if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
                    try {
                        val location = view.tag.toString().toInt()
                        performHapticFeedback()
                        homeAppClicked(location)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onLongClick(view: View): Boolean {
        // Handle traditional home app long clicks
        if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
            try {
                val location = view.tag.toString().toInt()
                showAppList(location)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun initClickListeners() {
        binding.setAsDefaultLauncher.setOnClickListener(this)
        binding.clock.setOnClickListener(this)
        binding.date.setOnClickListener(this)
    }

    private fun showLongPressToast() {
        Toast.makeText(this, "Long press to select app", Toast.LENGTH_SHORT).show()
    }

    private fun populateHomeApps() {
        val homeAppsNum = prefs.homeAppsNum

        // Hide all home apps first
        binding.homeApp1.visibility = View.GONE
        binding.homeApp2.visibility = View.GONE
        binding.homeApp3.visibility = View.GONE
        binding.homeApp4.visibility = View.GONE
        binding.homeApp5.visibility = View.GONE
        binding.homeApp6.visibility = View.GONE
        binding.homeApp7.visibility = View.GONE
        binding.homeApp8.visibility = View.GONE

        if (homeAppsNum == 0) return

        // Show and populate apps based on the number
        binding.homeApp1.visibility = View.VISIBLE
        val appPackage1 = prefs.getAppPackage(1)
        if (appPackage1.isNotEmpty()) {
            binding.homeApp1.text = prefs.getCustomAppName(appPackage1, prefs.getAppName(1))
        } else {
            binding.homeApp1.text = prefs.getAppName(1)
        }
        if (homeAppsNum == 1) return

        binding.homeApp2.visibility = View.VISIBLE
        val appPackage2 = prefs.getAppPackage(2)
        if (appPackage2.isNotEmpty()) {
            binding.homeApp2.text = prefs.getCustomAppName(appPackage2, prefs.getAppName(2))
        } else {
            binding.homeApp2.text = prefs.getAppName(2)
        }
        if (homeAppsNum == 2) return

        binding.homeApp3.visibility = View.VISIBLE
        val appPackage3 = prefs.getAppPackage(3)
        if (appPackage3.isNotEmpty()) {
            binding.homeApp3.text = prefs.getCustomAppName(appPackage3, prefs.getAppName(3))
        } else {
            binding.homeApp3.text = prefs.getAppName(3)
        }
        if (homeAppsNum == 3) return

        binding.homeApp4.visibility = View.VISIBLE
        val appPackage4 = prefs.getAppPackage(4)
        if (appPackage4.isNotEmpty()) {
            binding.homeApp4.text = prefs.getCustomAppName(appPackage4, prefs.getAppName(4))
        } else {
            binding.homeApp4.text = prefs.getAppName(4)
        }
        if (homeAppsNum == 4) return

        binding.homeApp5.visibility = View.VISIBLE
        val appPackage5 = prefs.getAppPackage(5)
        if (appPackage5.isNotEmpty()) {
            binding.homeApp5.text = prefs.getCustomAppName(appPackage5, prefs.getAppName(5))
        } else {
            binding.homeApp5.text = prefs.getAppName(5)
        }
        if (homeAppsNum == 5) return

        binding.homeApp6.visibility = View.VISIBLE
        val appPackage6 = prefs.getAppPackage(6)
        if (appPackage6.isNotEmpty()) {
            binding.homeApp6.text = prefs.getCustomAppName(appPackage6, prefs.getAppName(6))
        } else {
            binding.homeApp6.text = prefs.getAppName(6)
        }
        if (homeAppsNum == 6) return

        binding.homeApp7.visibility = View.VISIBLE
        val appPackage7 = prefs.getAppPackage(7)
        if (appPackage7.isNotEmpty()) {
            binding.homeApp7.text = prefs.getCustomAppName(appPackage7, prefs.getAppName(7))
        } else {
            binding.homeApp7.text = prefs.getAppName(7)
        }
        if (homeAppsNum == 7) return

        binding.homeApp8.visibility = View.VISIBLE
        val appPackage8 = prefs.getAppPackage(8)
        if (appPackage8.isNotEmpty()) {
            binding.homeApp8.text = prefs.getCustomAppName(appPackage8, prefs.getAppName(8))
        } else {
            binding.homeApp8.text = prefs.getAppName(8)
        }
    }

    private fun backToHome() {
        binding.appDrawerLayout.visibility = View.GONE
        binding.homeAppsLayout.visibility = View.VISIBLE
        appAdapter.setFlag(FLAG_LAUNCH_APP)
        hideKeyboard()
        // appListView.setSelectionAfterHeaderView() // Not needed for RecyclerView or different API
        binding.appListView.scrollToPosition(0)
        checkForDefaultLauncher()

        // Remove blur effect when returning to home
        removeBlurFromBackground()
    }

    private fun refreshAppsList() {
        viewModel.loadApps()
    }

    private fun showAppList(flag: Int) {
        binding.setAsDefaultLauncher.visibility = View.GONE
        binding.search.text.clear()
        appAdapter.setFlag(flag)
        binding.homeAppsLayout.visibility = View.GONE
        binding.appDrawerLayout.visibility = View.VISIBLE

        // Apply the same alignment as home screen to app drawer
        setAppDrawerAlignment()

        // Apply blur effect to app drawer background
        applyBlurToBackground()

        // Show keyboard with a delay to ensure the layout is fully rendered
        if (prefs.keyboardAutoShow) {
            binding.appDrawerLayout.postDelayed({
                showKeyboard()
            }, 100) // Small delay to ensure the app drawer is fully visible
        }
    }

    private fun showKeyboard() {
        binding.search.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        binding.search.clearFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.search.windowToken, 0)
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
                    binding.appDrawerBackground.setRenderEffect(blurEffect)
                    binding.appDrawerBackground.setBackgroundColor(Color.argb(100, 255, 255, 255)) // White semi-transparent layer
                }
            } catch (e: Exception) {
                // Fallback if RenderEffect fails
                applyFallbackBlur()
            }
        } else {
            // Remove any existing blur effect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.appDrawerBackground.setRenderEffect(null)
            }
            applyFallbackBlur()
        }
    }

    private fun applyFallbackBlur() {
        // Always apply blur for better user experience
        binding.appDrawerBackground.setBackgroundColor(Color.argb(180, 0, 0, 0)) // Dark semi-transparent layer
    }

    private fun removeBlurFromBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.appDrawerBackground.setRenderEffect(null)
        }
        binding.appDrawerBackground.setBackgroundColor(Color.TRANSPARENT)
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
        binding.search.text.clear()
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
        // Update traditional app list if needed
        if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
            populateHomeApps()
        }
    }

    private fun checkForDefaultLauncher() {
        if (BuildConfig.APPLICATION_ID == getDefaultLauncherPackage()) {
            binding.setAsDefaultLauncher.visibility = View.GONE
        } else {
            binding.setAsDefaultLauncher.visibility = View.VISIBLE
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
                performHapticFeedback()
                if (flag == FLAG_LAUNCH_APP) {
                    prepareToLaunchApp(appModel)
                } else {
                    setHomeApp(appModel, flag)
                }
            }

            override fun appLongPress(appModel: AppModel) {
                performHapticFeedback()
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

    private fun getScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            private var onTop = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) { // dragging
                    onTop = !recyclerView.canScrollVertically(-1)
                    if (onTop) hideKeyboard()
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) { // stopped
                    if (!recyclerView.canScrollVertically(1)) {
                        hideKeyboard()
                    } else if (!recyclerView.canScrollVertically(-1)) {
                        if (onTop) {
                            backToHome()
                        } else {
                            showKeyboard()
                        }
                    }
                }
            }
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
                // Show app drawer instead of planet launcher
                showAppList(FLAG_LAUNCH_APP)
            }

            override fun onSwipeDown() {
                super.onSwipeDown()
                expandNotificationDrawer()
            }

            override fun onLongClick() {
                super.onLongClick()
                // Only show settings on long press in list mode
                if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
            }
        }
    }





    private fun updateHomeTextSizes() {
        // Update clock and date text sizes
        val scaledClockSize = resources.getDimension(R.dimen.text_clock) * prefs.textSizeScale
        binding.clock.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledClockSize)
        binding.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledClockSize * 0.4f) // Date is smaller

        // Update text sizes for traditional home apps if needed
        if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
            val scaledTextSize = resources.getDimension(R.dimen.text_large) * prefs.textSizeScale

            binding.homeApp1.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp2.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp3.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp4.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp5.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp6.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp7.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
            binding.homeApp8.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSize)
        }
    }

    private fun updateHomeFontFamily() {
        // Update font family for clock and date
        val fontFamily = prefs.getFontFamily()
        binding.clock.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        binding.date.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)

        // Update search field font family
        binding.search.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)

        // Update font family for traditional home apps if needed
        if (prefs.navigationStyle == Prefs.Constants.NavigationStyle.LIST) {
            binding.homeApp1.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp2.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp3.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp4.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp5.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp6.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp7.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
            binding.homeApp8.typeface = android.graphics.Typeface.create(fontFamily, android.graphics.Typeface.NORMAL)
        }
    }

    private fun setHomeAlignment() {
        val horizontalGravity = prefs.homeAlignment
        val verticalGravity = if (prefs.homeBottomAlignment) Gravity.BOTTOM else Gravity.CENTER_VERTICAL

        binding.homeAppsLayout.gravity = horizontalGravity or verticalGravity

        // Apply alignment to individual app text views
        binding.homeApp1.gravity = horizontalGravity
        binding.homeApp2.gravity = horizontalGravity
        binding.homeApp3.gravity = horizontalGravity
        binding.homeApp4.gravity = horizontalGravity
        binding.homeApp5.gravity = horizontalGravity
        binding.homeApp6.gravity = horizontalGravity
        binding.homeApp7.gravity = horizontalGravity
        binding.homeApp8.gravity = horizontalGravity
    }

    private fun setAppDrawerAlignment() {
        val horizontalGravity = prefs.homeAlignment

        // Apply alignment to app drawer content container
        val appDrawerContent = binding.appDrawerLayout.getChildAt(1) as LinearLayout
        appDrawerContent.gravity = horizontalGravity or Gravity.TOP

        // Apply alignment to search field
        binding.search.gravity = horizontalGravity
        binding.search.textAlignment = when (horizontalGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> View.TEXT_ALIGNMENT_TEXT_START
            Gravity.RIGHT -> View.TEXT_ALIGNMENT_TEXT_END
            else -> View.TEXT_ALIGNMENT_GRAVITY
        }

        // Apply alignment to RecyclerView by setting its layout parameters
        val layoutParams = binding.appListView.layoutParams as LinearLayout.LayoutParams
        layoutParams.gravity = horizontalGravity
        binding.appListView.layoutParams = layoutParams
    }
    
    private fun initBatteryMonitoring() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    updateBatteryStatus()
                }
            }
        }
        
        // Register the battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        
        // Initial battery status update
        updateBatteryStatus()
    }
    
    private fun updateBatteryStatus() {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        if (batteryStatus != null) {
            val level: Int = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            val batteryPct: Float = if (level != -1 && scale != -1) {
                level / scale.toFloat()
            } else {
                0f
            }

            val isCharging: Boolean = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) == android.os.BatteryManager.BATTERY_STATUS_FULL

            val batteryText = if (isCharging) {
                "${(batteryPct * 100).toInt()}% ⚡"
            } else {
                "${(batteryPct * 100).toInt()}%"
            }

            binding.batteryIndicator.text = batteryText
        }
    }

    private fun updateScreenTimeDisplay() {
        if (prefs.screenTimeEnabled) {
            if (ScreenTimeHelper.isUsageAccessPermissionGranted(this)) {
                binding.tvScreenTime.visibility = View.VISIBLE
                binding.tvScreenTime.text = ScreenTimeHelper.getTodaysScreenTime(this, prefs)
            } else {
                binding.tvScreenTime.visibility = View.GONE
            }
        } else {
            binding.tvScreenTime.visibility = View.GONE
        }
    }
}
