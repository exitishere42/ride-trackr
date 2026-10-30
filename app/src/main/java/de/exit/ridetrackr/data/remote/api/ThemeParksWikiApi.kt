package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface ThemeParksWikiApi {
    @GET("destinations")
    suspend fun getDestinations(): DestinationsResponse

    @GET("entity/{entityId}/live")
    suspend fun getLiveWaitTimes(@Path("entityId") entityId: String): EntityLiveResponse
}

@Serializable
data class DestinationsResponse(
    val destinations: List<DestinationDto> = emptyList()
)

@Serializable
data class DestinationDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val parks: List<ParkDto> = emptyList()
)

@Serializable
data class ParkDto(
    val id: String,
    val name: String
)

@Serializable
data class EntityLiveResponse(
    val id: String,
    val name: String,
    val entityType: String? = null,
    val timezone: String? = null,
    val liveData: List<LiveItemDto> = emptyList()
)

@Serializable
data class LiveItemDto(
    val id: String,
    val name: String,
    val entityType: String? = null,
    val status: String? = null,
    val queue: QueueDto? = null,
    val lastUpdated: String? = null
)

@Serializable
data class QueueDto(
    val STANDBY: StandbyQueueDto? = null
)

@Serializable
data class StandbyQueueDto(
    val waitTime: Int? = null
)
