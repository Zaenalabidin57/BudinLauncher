package app.budinlauncher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.budinlauncher.domain.repository.SettingsRepository
import app.budinlauncher.domain.repository.UsageStatsRepository
import app.budinlauncher.domain.usecase.GetScreenTimeUseCase
import app.budinlauncher.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val getScreenTimeUseCase: GetScreenTimeUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val showSystemApps = settingsRepository.getBooleanSetting("show_system_apps", false)
                val enableScreenTime = settingsRepository.getBooleanSetting("enable_screen_time", true)
                
                _uiState.value = _uiState.value.copy(
                    showSystemApps = showSystemApps,
                    enableScreenTime = enableScreenTime
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.observeBooleanSetting("show_system_apps", false),
                settingsRepository.observeBooleanSetting("enable_screen_time", true)
            ) { showSystemApps, enableScreenTime ->
                _uiState.value = _uiState.value.copy(
                    showSystemApps = showSystemApps,
                    enableScreenTime = enableScreenTime
                )
            }
        }
    }

    fun updateShowSystemApps(show: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.updateBooleanSetting("show_system_apps", show).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(showSystemApps = show)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    fun updateEnableScreenTime(enable: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.updateBooleanSetting("enable_screen_time", enable).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(enableScreenTime = enable)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(error = error.message)
                    }
                )
            }
        }
    }

    fun loadScreenTimeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingScreenTime = true)
            
            getScreenTimeUseCase().collect { result ->
                result.fold(
                    onSuccess = { usageStats ->
                        _uiState.value = _uiState.value.copy(
                            screenTimeData = usageStats,
                            isLoadingScreenTime = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingScreenTime = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SettingsUiState(
    val showSystemApps: Boolean = false,
    val enableScreenTime: Boolean = true,
    val screenTimeData: List<app.budinlauncher.domain.model.UsageStats> = emptyList(),
    val isLoadingScreenTime: Boolean = false,
    val error: String? = null
)