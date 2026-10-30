package de.exit.ridetrackr.data.model

sealed interface AppUpdateStatus {
    data object Idle : AppUpdateStatus
    data object Checking : AppUpdateStatus
    data class UpToDate(val currentVersion: String) : AppUpdateStatus
    data class UpdateAvailable(
        val currentVersion: String,
        val newVersion: String,
        val releaseName: String?,
        val releaseNotes: String?,
        val downloadUrl: String,
        val apkSizeMb: Double?
    ) : AppUpdateStatus
    data class Error(val message: String) : AppUpdateStatus
}
