package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.model.AppUpdateStatus
import de.exit.ridetrackr.data.remote.api.GitHubReleasesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val gitHubReleasesApi: GitHubReleasesApi
) {

    suspend fun checkForUpdate(currentVersion: String): AppUpdateStatus {
        return try {
            val release = gitHubReleasesApi.getLatestRelease()
            val rawTag = release.tagName.trim()
            val cleanLatest = rawTag.removePrefix("v").removePrefix("V")
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")

            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            val downloadUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl
            val apkSizeMb = apkAsset?.let {
                if (it.size > 0) Math.round((it.size / (1024.0 * 1024.0)) * 10.0) / 10.0 else null
            }

            if (isNewerVersion(cleanCurrent, cleanLatest)) {
                AppUpdateStatus.UpdateAvailable(
                    currentVersion = cleanCurrent,
                    newVersion = cleanLatest,
                    releaseName = release.name,
                    releaseNotes = release.body,
                    downloadUrl = downloadUrl,
                    apkSizeMb = apkSizeMb
                )
            } else {
                AppUpdateStatus.UpToDate(currentVersion = cleanCurrent)
            }
        } catch (e: Exception) {
            AppUpdateStatus.Error(message = e.localizedMessage ?: "Verbindung zu GitHub fehlgeschlagen")
        }
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val curr = currentParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (lat > curr) return true
            if (lat < curr) return false
        }
        return false
    }
}
