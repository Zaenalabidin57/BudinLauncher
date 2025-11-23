package app.budinlauncher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.budinlauncher.domain.model.AppInfo
import app.budinlauncher.domain.usecase.GetAppsUseCase
import app.budinlauncher.domain.usecase.LaunchAppUseCase
import app.budinlauncher.domain.usecase.SearchAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAppsUseCase: GetAppsUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getAppsUseCase().collect { result ->
                result.fold(
                    onSuccess = { apps ->
                        _uiState.value = _uiState.value.copy(
                            apps = apps,
                            filteredApps = apps,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun searchApps(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            searchAppsUseCase(query).collect { result ->
                result.fold(
                    onSuccess = { apps ->
                        _uiState.value = _uiState.value.copy(
                            filteredApps = apps,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            launchAppUseCase(packageName).collect { result ->
                result.fold(
                    onSuccess = { success ->
                        if (!success) {
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to launch app"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
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

data class MainUiState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)