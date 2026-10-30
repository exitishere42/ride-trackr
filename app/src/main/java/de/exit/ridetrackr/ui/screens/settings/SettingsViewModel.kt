package de.exit.ridetrackr.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.exit.ridetrackr.BuildConfig
import de.exit.ridetrackr.data.model.AppUpdateStatus
import de.exit.ridetrackr.data.repository.SettingsRepository
import de.exit.ridetrackr.data.repository.UpdateRepository
import de.exit.ridetrackr.service.WaitTimeAlertWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val alertsEnabled: Boolean = false,
    val alertThresholdMinutes: Int = 30,
    val colorAccent: String = "TEAL",
    val language: String = "DE"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    val currentVersion: String = BuildConfig.VERSION_NAME

    private val _updateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.Idle)
    val updateStatus: StateFlow<AppUpdateStatus> = _updateStatus.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.alertsEnabled,
        settingsRepository.alertThresholdMinutes,
        settingsRepository.colorAccent,
        settingsRepository.language
    ) { enabled, threshold, accent, lang ->
        SettingsUiState(
            alertsEnabled = enabled,
            alertThresholdMinutes = threshold,
            colorAccent = accent,
            language = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAlertsEnabled(enabled)
            updateWorkManager(enabled)
        }
    }

    fun setAlertThreshold(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setAlertThresholdMinutes(minutes)
        }
    }

    fun setColorAccent(accent: String) {
        viewModelScope.launch {
            settingsRepository.setColorAccent(accent)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateStatus.value = AppUpdateStatus.Checking
            val result = updateRepository.checkForUpdate(currentVersion)
            _updateStatus.value = result
        }
    }

    private fun updateWorkManager(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val alertRequest = PeriodicWorkRequestBuilder<WaitTimeAlertWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            workManager.enqueueUniquePeriodicWork(
                "wait_time_alerts",
                ExistingPeriodicWorkPolicy.UPDATE,
                alertRequest
            )
        } else {
            workManager.cancelUniqueWork("wait_time_alerts")
        }
    }
}
