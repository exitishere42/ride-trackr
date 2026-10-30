package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubCloudDbApi {

    @GET("exitishere42/ride-trackr/main/data/live_reports.json")
    suspend fun getLiveReportsRaw(): Map<String, Map<String, LiveReportDto>>

    @GET("repos/exitishere42/ride-trackr/contents/data/live_reports.json")
    suspend fun getFileMetadata(
        @Header("Authorization") authHeader: String? = null
    ): GitHubFileContentDto

    @PUT("repos/exitishere42/ride-trackr/contents/data/live_reports.json")
    suspend fun updateLiveReports(
        @Header("Authorization") authHeader: String,
        @Body request: GitHubUpdateContentRequestDto
    ): GitHubUpdateResponseDto
}

@Serializable
data class LiveReportDto(
    val parkId: String,
    val attractionId: String,
    val attractionName: String,
    val waitTimeMinutes: Int,
    val reportedAtMillis: Long,
    val note: String? = null,
    val deviceId: String? = null
)

@Serializable
data class GitHubFileContentDto(
    val sha: String,
    val content: String? = null,
    val encoding: String? = null
)

@Serializable
data class GitHubUpdateContentRequestDto(
    val message: String,
    val content: String, // base64 encoded
    val sha: String,
    val branch: String = "main"
)

@Serializable
data class GitHubUpdateResponseDto(
    val content: GitHubFileContentDto? = null
)
