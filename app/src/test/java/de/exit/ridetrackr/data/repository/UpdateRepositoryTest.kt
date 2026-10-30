package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.model.AppUpdateStatus
import de.exit.ridetrackr.data.remote.api.GitHubReleaseAssetDto
import de.exit.ridetrackr.data.remote.api.GitHubReleaseDto
import de.exit.ridetrackr.data.remote.api.GitHubReleasesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateRepositoryTest {

    private class FakeGitHubReleasesApi(
        var releaseToReturn: GitHubReleaseDto? = null,
        var shouldThrow: Boolean = false
    ) : GitHubReleasesApi {
        override suspend fun getLatestRelease(): GitHubReleaseDto {
            if (shouldThrow) throw RuntimeException("Network timeout")
            return releaseToReturn ?: GitHubReleaseDto(
                tagName = "v1.2.0",
                name = "Ride Trackr v1.2.0",
                htmlUrl = "https://github.com/exitishere42/ride-trackr/releases/tag/v1.2.0"
            )
        }
    }

    @Test
    fun testIsNewerVersion() {
        val repo = UpdateRepository(FakeGitHubReleasesApi())

        assertTrue(repo.isNewerVersion("1.2.0", "1.3.0"))
        assertTrue(repo.isNewerVersion("1.2.0", "1.2.1"))
        assertTrue(repo.isNewerVersion("1.9.5", "2.0.0"))

        assertFalse(repo.isNewerVersion("1.2.0", "1.2.0"))
        assertFalse(repo.isNewerVersion("1.3.0", "1.2.0"))
        assertFalse(repo.isNewerVersion("2.0.0", "1.9.9"))
    }

    @Test
    fun testCheckForUpdate_updateAvailable() = runBlocking {
        val fakeApi = FakeGitHubReleasesApi(
            releaseToReturn = GitHubReleaseDto(
                tagName = "v1.3.0",
                name = "Ride Trackr v1.3.0",
                body = "In-App Update Checker from GitHub Releases",
                htmlUrl = "https://github.com/exitishere42/ride-trackr/releases/tag/v1.3.0",
                assets = listOf(
                    GitHubReleaseAssetDto(
                        name = "ride-trackr-v1.3.0.apk",
                        size = 19433728L,
                        browserDownloadUrl = "https://github.com/exitishere42/ride-trackr/releases/download/v1.3.0/ride-trackr-v1.3.0.apk"
                    )
                )
            )
        )

        val repo = UpdateRepository(fakeApi)
        val status = repo.checkForUpdate("1.2.0")

        assertTrue("Expected UpdateAvailable status, got $status", status is AppUpdateStatus.UpdateAvailable)
        val update = status as AppUpdateStatus.UpdateAvailable
        assertEquals("1.3.0", update.newVersion)
        assertEquals("https://github.com/exitishere42/ride-trackr/releases/download/v1.3.0/ride-trackr-v1.3.0.apk", update.downloadUrl)
        assertEquals(18.5, update.apkSizeMb ?: 0.0, 0.5)
    }

    @Test
    fun testCheckForUpdate_upToDate() = runBlocking {
        val fakeApi = FakeGitHubReleasesApi(
            releaseToReturn = GitHubReleaseDto(
                tagName = "v1.2.0",
                name = "Ride Trackr v1.2.0",
                htmlUrl = "https://github.com/exitishere42/ride-trackr/releases/tag/v1.2.0"
            )
        )

        val repo = UpdateRepository(fakeApi)
        val status = repo.checkForUpdate("1.2.0")

        assertTrue("Expected UpToDate status, got $status", status is AppUpdateStatus.UpToDate)
        assertEquals("1.2.0", (status as AppUpdateStatus.UpToDate).currentVersion)
    }

    @Test
    fun testCheckForUpdate_errorHandling() = runBlocking {
        val fakeApi = FakeGitHubReleasesApi(shouldThrow = true)
        val repo = UpdateRepository(fakeApi)
        val status = repo.checkForUpdate("1.2.0")

        assertTrue("Expected Error status, got $status", status is AppUpdateStatus.Error)
        assertEquals("Network timeout", (status as AppUpdateStatus.Error).message)
    }
}
