package app.budinlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object AppMenuHelper {

    fun showAppMenu(context: Context, appModel: AppModel, onRename: (String) -> Unit, onUninstall: () -> Unit, onAppInfo: () -> Unit) {
        val options = arrayOf("Rename", "App info", "Uninstall")
        
        AlertDialog.Builder(context)
            .setTitle(appModel.appLabel)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameDialog(context, appModel.appLabel, onRename)
                    1 -> onAppInfo()
                    2 -> onUninstall()
                }
            }
            .show()
    }

    private fun showRenameDialog(context: Context, currentName: String, onRename: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        val input = android.widget.EditText(context)
        input.setText(currentName)
        input.selectAll()
        
        builder.setTitle("Rename app")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    onRename(newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun uninstallApp(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to uninstall app", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(context: Context, packageName: String, userHandle: UserHandle) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to open app info", Toast.LENGTH_SHORT).show()
        }
    }
}