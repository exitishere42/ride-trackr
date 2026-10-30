package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface GitHubReleasesApi {

    @GET("repos/exitishere42/ride-trackr/releases/latest")
    suspend fun getLatestRelease(): GitHubReleaseDto
}

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    val assets: List<GitHubReleaseAssetDto> = emptyList()
)

@Serializable
data class GitHubReleaseAssetDto(
    val name: String,
    val size: Long = 0,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)
