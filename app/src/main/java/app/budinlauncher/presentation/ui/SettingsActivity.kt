package app.budinlauncher.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.budinlauncher.databinding.ActivitySettingsNewBinding
import app.budinlauncher.presentation.adapter.ScreenTimeAdapter
import app.budinlauncher.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var screenTimeAdapter: ScreenTimeAdapter

    private lateinit var binding: ActivitySettingsNewBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewScreenTime.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = screenTimeAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.switchShowSystemApps.isChecked = uiState.showSystemApps
                binding.switchEnableScreenTime.isChecked = uiState.enableScreenTime
                binding.progressBarScreenTime.isVisible = uiState.isLoadingScreenTime
                binding.textViewError.text = uiState.error ?: ""
                binding.textViewError.isVisible = uiState.error != null

                if (!uiState.isLoadingScreenTime && uiState.error == null) {
                    screenTimeAdapter.updateUsageStats(uiState.screenTimeData)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.switchShowSystemApps.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowSystemApps(isChecked)
        }

        binding.switchEnableScreenTime.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateEnableScreenTime(isChecked)
        }

        binding.buttonLoadScreenTime.setOnClickListener {
            viewModel.loadScreenTimeData()
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}