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
    private lateinit var appDrawer: View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        findViewById<View>(R.id.layout_main).setOnTouchListener(getSwipeGestureListener(this))
        initClickListeners()

        prefs = Prefs(this)
        search = findViewById(R.id.search)
        homeAppsLayout = findViewById(R.id.home_apps_layout)
        appDrawer = findViewById(R.id.app_drawer_layout)

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
        refreshAppsList()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.set_as_default_launcher -> resetDefaultLauncher()
            R.id.clock -> startActivity(Intent(Intent(AlarmClock.ACTION_SHOW_ALARMS)))
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

        homeApp1.setOnClickListener(this)
        homeApp2.setOnClickListener(this)
        homeApp3.setOnClickListener(this)
        homeApp4.setOnClickListener(this)
        homeApp5.setOnClickListener(this)
        homeApp6.setOnClickListener(this)

        homeApp1.setOnLongClickListener(this)
        homeApp2.setOnLongClickListener(this)
        homeApp3.setOnLongClickListener(this)
        homeApp4.setOnLongClickListener(this)
        homeApp5.setOnLongClickListener(this)
        homeApp6.setOnLongClickListener(this)
    }

    private fun populateHomeApps() {
        homeApp1.text = prefs.getAppName(1)
        homeApp2.text = prefs.getAppName(2)
        homeApp3.text = prefs.getAppName(3)
        homeApp4.text = prefs.getAppName(4)
        homeApp5.text = prefs.getAppName(5)
        homeApp6.text = prefs.getAppName(6)
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
    }

    private fun refreshAppsList() {
        Thread {
            try {
                val apps = mutableListOf<AppModel>()
                val userManager = getSystemService(Context.USER_SERVICE) as UserManager
                val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                
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
                    app1.appLabel.compareTo(app2.appLabel, ignoreCase = true)
                }
                
                appList.clear()
                appList.addAll(apps)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAppList(flag: Int) {
        setDefaultLauncher.visibility = View.GONE
        showKeyboard()
        search.text.clear()
        appAdapter.setFlag(flag)
        homeAppsLayout.visibility = View.GONE
        appDrawer.visibility = View.VISIBLE
    }

    private fun showKeyboard() {
        search.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        search.clearFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(search.windowToken, 0)
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
        launchApp(appModel)
        backToHome()
        search.text.clear()
    }

    private fun homeAppClicked(location: Int) {
        if (prefs.getAppPackage(location).isEmpty()) {
            showLongPressToast()
        } else {
            launchApp(
                getAppModel(
                    prefs.getAppName(location),
                    prefs.getAppPackage(location),
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

    private fun getSwipeGestureListener(context: Context): OnSwipeTouchListener {
        return object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                startActivity(intent)
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                val intent = Intent(Intent.ACTION_DIAL)
                startActivity(intent)
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                showAppList(FLAG_LAUNCH_APP)
            }

            override fun onSwipeDown() {
                super.onSwipeDown()
                expandNotificationDrawer()
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

        private val allAppsList: List<AppModel> = filteredAppsList.toList()
        private var flag = 0

        private class ViewHolder {
            lateinit var appName: TextView
            lateinit var indicator: View
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
                view = inflater.inflate(R.layout.adapter_app, parent, false)
                viewHolder.appName = view.findViewById(R.id.app_name)
                viewHolder.indicator = view.findViewById(R.id.other_profile_indicator)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder
            }

            viewHolder.appName.tag = appModel
            viewHolder.appName.text = appModel.appLabel
            viewHolder.appName.setOnClickListener { view ->
                val clickedAppModel = view.tag as AppModel
                appClickListener.appClicked(clickedAppModel, flag)
            }
            viewHolder.appName.setOnLongClickListener { view ->
                val clickedAppModel = view.tag as AppModel
                appClickListener.appLongPress(clickedAppModel)
                true
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
}