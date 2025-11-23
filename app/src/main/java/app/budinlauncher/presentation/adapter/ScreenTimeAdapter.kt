package app.budinlauncher.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.budinlauncher.databinding.ItemScreenTimeBinding
import app.budinlauncher.domain.model.UsageStats
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScreenTimeAdapter @Inject constructor() : ListAdapter<UsageStats, ScreenTimeAdapter.ScreenTimeViewHolder>(ScreenTimeDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenTimeViewHolder {
        val binding = ItemScreenTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScreenTimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScreenTimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateUsageStats(usageStats: List<UsageStats>) {
        submitList(usageStats)
    }

    inner class ScreenTimeViewHolder(private val binding: ItemScreenTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(usageStats: UsageStats) {
            binding.textViewAppName.text = usageStats.appName
            binding.textViewPackageName.text = usageStats.packageName
            binding.textViewScreenTime.text = formatScreenTime(usageStats.totalTimeInForeground)
            binding.textViewLastUsed.text = formatLastUsed(usageStats.lastTimeUsed)
            binding.textViewLaunchCount.text = "${usageStats.launchCount} launches"
        }
        
        private fun formatScreenTime(milliseconds: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
            
            return when {
                hours > 0 -> String.format("%dh %dm", hours, minutes)
                minutes > 0 -> String.format("%dm %ds", minutes, seconds)
                else -> String.format("%ds", seconds)
            }
        }
        
        private fun formatLastUsed(timestamp: Long): String {
            return if (timestamp > 0) {
                timeFormat.format(Date(timestamp))
            } else {
                "Never"
            }
        }
    }
}

class ScreenTimeDiffCallback : DiffUtil.ItemCallback<UsageStats>() {
    override fun areItemsTheSame(oldItem: UsageStats, newItem: UsageStats): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: UsageStats, newItem: UsageStats): Boolean {
        return oldItem == newItem
    }
}