package app.budinlauncher.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.budinlauncher.databinding.ItemAppBinding
import app.budinlauncher.domain.model.AppInfo
import javax.inject.Inject

class AppAdapter @Inject constructor() : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    private var onAppClickListener: ((AppInfo) -> Unit)? = null
    private var onAppLongClickListener: ((AppInfo) -> Unit)? = null

    fun setOnAppClickListener(listener: (AppInfo) -> Unit) {
        onAppClickListener = listener
    }

    fun setOnAppLongClickListener(listener: (AppInfo) -> Unit) {
        onAppLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateApps(apps: List<AppInfo>) {
        submitList(apps)
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(appInfo: AppInfo) {
            binding.textViewAppName.text = appInfo.label
            binding.imageViewAppIcon.setImageDrawable(appInfo.icon)
            
            binding.root.setOnClickListener {
                onAppClickListener?.invoke(appInfo)
            }
            
            binding.root.setOnLongClickListener {
                onAppLongClickListener?.invoke(appInfo)
                true
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem == newItem
    }
}