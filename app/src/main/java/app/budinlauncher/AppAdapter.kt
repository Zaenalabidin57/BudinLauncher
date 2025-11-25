package app.budinlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import app.budinlauncher.databinding.AdapterAppMenuBinding

class AppAdapter(
    private val context: Context,
    private var filteredAppsList: List<AppModel>,
    private val appClickListener: MainActivity.AppClickListener
) : RecyclerView.Adapter<AppAdapter.ViewHolder>(), Filterable {

    private var allAppsList: List<AppModel> = filteredAppsList.toList()
    private var flag = 0

    fun updateAppList(newAppList: List<AppModel>) {
        filteredAppsList = newAppList
        allAppsList = newAppList.toList()
        notifyDataSetChanged()
    }

    fun setFlag(flag: Int) {
        this.flag = flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterAppMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appModel = filteredAppsList[position]
        holder.bind(appModel)
    }

    override fun getItemCount(): Int = filteredAppsList.size

    inner class ViewHolder(private val binding: AdapterAppMenuBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appModel: AppModel) {
            binding.appName.tag = appModel
            binding.appName.text = getCustomAppName(context, appModel.appPackage, appModel.appLabel)

            // Set font family
            val prefs = Prefs(context)
            binding.appName.typeface = android.graphics.Typeface.create(prefs.getFontFamily(), android.graphics.Typeface.NORMAL)

            // Set app icon
            if (prefs.showAppIcons) {
                try {
                    val icon = context.packageManager.getApplicationIcon(appModel.appPackage)
                    val iconSize = (48 * context.resources.displayMetrics.density).toInt()
                    icon.setBounds(0, 0, iconSize, iconSize)
                    binding.appName.setCompoundDrawables(icon, null, null, null)
                } catch (e: Exception) {
                    val defaultIcon = context.getDrawable(android.R.drawable.sym_def_app_icon)
                    val iconSize = (48 * context.resources.displayMetrics.density).toInt()
                    defaultIcon?.setBounds(0, 0, iconSize, iconSize)
                    binding.appName.setCompoundDrawables(defaultIcon, null, null, null)
                }
            } else {
                binding.appName.setCompoundDrawables(null, null, null, null)
            }

            // User handle indicator
            if (appModel.userHandle == android.os.Process.myUserHandle()) {
                binding.otherProfileIndicator.visibility = View.GONE
            } else {
                binding.otherProfileIndicator.visibility = View.VISIBLE
            }

            // Reset menu visibility
            hideAppMenu()
            hideRenameDialog()

            // Click listeners
            binding.appName.setOnClickListener {
                appClickListener.appClicked(appModel, flag)
            }

            binding.appName.setOnLongClickListener {
                showAppMenu(appModel)
                true
            }

            binding.appUninstall.setOnClickListener {
                uninstallApp(appModel)
                hideAppMenu()
            }

            binding.appRename.setOnClickListener {
                showRenameDialog(appModel)
            }

            binding.appInfo.setOnClickListener {
                appClickListener.appLongPress(appModel)
                hideAppMenu()
            }

            binding.appClose.setOnClickListener {
                hideAppMenu()
            }

            binding.saveRename.setOnClickListener {
                saveAppName(appModel)
            }

            binding.closeRename.setOnClickListener {
                hideRenameDialog()
            }

            // Set up enter key listener for rename EditText
            binding.renameEditText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event?.action == android.view.KeyEvent.ACTION_DOWN && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                    saveAppName(appModel)
                    true
                } else {
                    false
                }
            }
            
            // Auto-launch if single result and flag is 0 (launch app)
            // Note: This might be risky in onBindViewHolder if the list updates frequently, 
            // but keeping it to match original behavior.
            if (flag == 0 && filteredAppsList.size == 1) {
                 // We should probably not trigger click here directly to avoid infinite loops or side effects during layout
                 // But original code did: if (flag == 0 && count == 1) appClickListener.appClicked(appModel, flag)
                 // Let's defer it slightly or just leave it. 
                 // Ideally this logic belongs in the filtering completion or activity.
            }
        }

        private fun showAppMenu(appModel: AppModel) {
            binding.appName.visibility = View.INVISIBLE
            binding.appMenuLayout.visibility = View.VISIBLE

            val isSystemApp = isSystemApp(context, appModel.appPackage)
            binding.appUninstall.alpha = if (isSystemApp) 0.5f else 1.0f
            binding.appUninstall.isEnabled = !isSystemApp
        }

        private fun hideAppMenu() {
            binding.appMenuLayout.visibility = View.GONE
            binding.appName.visibility = View.VISIBLE
        }

        private fun showRenameDialog(appModel: AppModel) {
            binding.renameEditText.setText(getCustomAppName(context, appModel.appPackage, appModel.appLabel))
            binding.renameEditText.hint = appModel.appLabel
            binding.renameEditText.setSelectAllOnFocus(true)
            binding.renameEditText.requestFocus()
            binding.renameLayout.visibility = View.VISIBLE
            binding.appMenuLayout.visibility = View.GONE

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.renameEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        private fun hideRenameDialog() {
            binding.renameLayout.visibility = View.GONE
            binding.appName.visibility = View.VISIBLE

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.renameEditText.windowToken, 0)
        }

        private fun saveAppName(appModel: AppModel) {
            val newName = binding.renameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                setCustomAppName(context, appModel.appPackage, newName)
                binding.appName.text = newName

                // Refresh the entire app list to update sorting and display
                notifyDataSetChanged()
            }
            hideRenameDialog()
        }
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
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                val filteredApps = mutableListOf<AppModel>()

                if (constraint.isBlank()) {
                    filteredApps.addAll(allAppsList)
                } else {
                    val lowerCaseConstraint = constraint.toString().lowercase().trim()
                    for (app in allAppsList) {
                        val customAppName = getCustomAppName(context, app.appPackage, app.appLabel)
                        if (app.appLabel.lowercase().contains(lowerCaseConstraint) ||
                            customAppName.lowercase().contains(lowerCaseConstraint)) {
                            filteredApps.add(app)
                        }
                    }
                }

                results.count = filteredApps.size
                results.values = filteredApps
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filteredAppsList = results.values as List<AppModel>
                notifyDataSetChanged()
                
                // Handle auto-launch if single result here instead of onBindViewHolder
                if (flag == 0 && filteredAppsList.size == 1 && constraint.isNotEmpty()) {
                     // Triggering click from here is tricky as we don't have the view or position easily
                     // But we can call the listener directly
                     appClickListener.appClicked(filteredAppsList[0], flag)
                }
            }
        }
    }
}
