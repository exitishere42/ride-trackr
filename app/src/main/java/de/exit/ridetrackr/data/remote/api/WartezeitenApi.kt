package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header

interface WartezeitenApi {
    @GET("v1/parks")
    suspend fun getParks(
        @Header("language") language: String = "de"
    ): List<WartezeitenParkDto>

    @GET("v1/waitingtimes")
    suspend fun getWaitingTimes(
        @Header("park") parkId: String,
        @Header("language") language: String = "de"
    ): List<WartezeitenAttractionDto>

    @GET("v1/crowdlevel")
    suspend fun getCrowdLevel(
        @Header("park") parkId: String = "europapark",
        @Header("language") language: String = "de"
    ): WartezeitenCrowdLevelDto
}

@Serializable
data class WartezeitenParkDto(
    val id: String,
    val uuid: String? = null,
    val name: String,
    val land: String? = null,
    val isOpen: Boolean? = true
)

@Serializable
data class WartezeitenAttractionDto(
    val id: String = "",
    val code: String? = null,
    val uuid: String? = null,
    val name: String,
    @SerialName("waitingtime")
    val waitTime: Int? = null,
    val status: String? = null,
    @SerialName("datetime")
    val lastUpdated: String? = null
) {
    fun resolveId(): String = if (id.isNotBlank()) id else uuid ?: code ?: name
}

@Serializable
data class WartezeitenCrowdLevelDto(
    @SerialName("crowd_level")
    val crowdLevel: Double? = null,
    val timestamp: String? = null
)
