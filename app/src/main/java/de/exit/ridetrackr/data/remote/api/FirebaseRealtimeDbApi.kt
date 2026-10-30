package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FirebaseRealtimeDbApi {

    @GET("reports/{parkId}.json")
    suspend fun getParkReports(
        @Path("parkId") parkId: String
    ): Map<String, FirebaseReportDto>?

    @PUT("reports/{parkId}/{attractionId}.json")
    suspend fun submitReport(
        @Path("parkId") parkId: String,
        @Path("attractionId") attractionId: String,
        @Body report: FirebaseReportDto
    ): FirebaseReportDto

    @GET("reports.json")
    suspend fun getAllReports(): Map<String, Map<String, FirebaseReportDto>>?
}

@Serializable
data class FirebaseReportDto(
    val parkId: String,
    val attractionId: String,
    val attractionName: String,
    val waitTimeMinutes: Int,
    val reportedAtMillis: Long,
    val note: String? = null,
    val deviceId: String? = null
)
