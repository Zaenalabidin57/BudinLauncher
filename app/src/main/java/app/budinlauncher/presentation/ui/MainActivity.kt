package app.budinlauncher.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.budinlauncher.databinding.ActivityMainNewBinding
import app.budinlauncher.domain.model.AppInfo
import app.budinlauncher.presentation.adapter.AppAdapter
import app.budinlauncher.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appAdapter: AppAdapter

    private lateinit var binding: ActivityMainNewBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupObservers()
        setupClickListeners()
        setupBackPressed()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appAdapter
        }

        appAdapter.setOnAppClickListener { appInfo ->
            onAppClicked(appInfo)
        }

        appAdapter.setOnAppLongClickListener { appInfo ->
            onAppLongClicked(appInfo)
        }
    }

    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchApps(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.progressBar.isVisible = uiState.isLoading
                binding.textViewError.isVisible = uiState.error != null
                binding.textViewError.text = uiState.error ?: ""

                if (!uiState.isLoading && uiState.error == null) {
                    appAdapter.updateApps(uiState.filteredApps)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.buttonRefresh.setOnClickListener {
            viewModel.searchApps("")
        }
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.editTextSearch.text?.toString().isNullOrEmpty()) {
                    binding.editTextSearch.text?.clear()
                } else {
                    finish()
                }
            }
        })
    }

    fun onAppClicked(appInfo: AppInfo) {
        viewModel.launchApp(appInfo.packageName)
    }

    fun onAppLongClicked(appInfo: AppInfo) {
        // Show app info or options
        Toast.makeText(this, "Long clicked: ${appInfo.label}", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTextSearch.windowToken, 0)
    }
}