package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.engine.PlausibilityEngine
import de.exit.ridetrackr.data.engine.TripsdrillWaitTimeEngine
import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.remote.api.DestinationsResponse
import de.exit.ridetrackr.data.remote.api.EntityLiveResponse
import de.exit.ridetrackr.data.remote.api.ThemeParksWikiApi
import de.exit.ridetrackr.data.remote.api.WartezeitenApi
import de.exit.ridetrackr.data.remote.api.WartezeitenAttractionDto
import de.exit.ridetrackr.data.remote.api.WartezeitenCrowdLevelDto
import de.exit.ridetrackr.data.remote.api.WartezeitenParkDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripsdrillAttractionsTest {

    private val fakeThemeParks = object : ThemeParksWikiApi {
        override suspend fun getDestinations(): DestinationsResponse = DestinationsResponse()
        override suspend fun getLiveWaitTimes(entityId: String): EntityLiveResponse {
            throw RuntimeException("404 Not Found")
        }
    }

    private val fakeWartezeiten = object : WartezeitenApi {
        override suspend fun getParks(language: String): List<WartezeitenParkDto> = emptyList()
        override suspend fun getWaitingTimes(parkId: String, language: String): List<WartezeitenAttractionDto> {
            throw RuntimeException("404 Not Found")
        }
        override suspend fun getCrowdLevel(parkId: String, language: String): WartezeitenCrowdLevelDto {
            return WartezeitenCrowdLevelDto(crowdLevel = 65.0)
        }
    }

    private val fakeUserReportsDao = object : UserReportsDao {
        val list = mutableListOf<UserWaitTimeReportEntity>()
        override suspend fun insertReport(report: UserWaitTimeReportEntity): Long = 1L
        override suspend fun getRecentReports(attractionId: String, minTimestamp: Long): List<UserWaitTimeReportEntity> = emptyList()
        override fun observeLatestReport(attractionId: String): Flow<UserWaitTimeReportEntity?> = flowOf(null)
        override suspend fun getLatestReport(attractionId: String): UserWaitTimeReportEntity? = null
        override suspend fun getRecentParkReports(parkId: String, minTimestamp: Long): List<UserWaitTimeReportEntity> = list
        override fun observeAllReports(): Flow<List<UserWaitTimeReportEntity>> = flowOf(list)
        override suspend fun deleteReport(id: Long) {}
        override suspend fun pruneOldReports(olderThanMillis: Long) {}
    }

    private val fakeFirebaseApi = object : de.exit.ridetrackr.data.remote.api.FirebaseRealtimeDbApi {
        override suspend fun getParkReports(parkId: String): Map<String, de.exit.ridetrackr.data.remote.api.FirebaseReportDto>? = null
        override suspend fun submitReport(
            parkId: String,
            attractionId: String,
            report: de.exit.ridetrackr.data.remote.api.FirebaseReportDto
        ): de.exit.ridetrackr.data.remote.api.FirebaseReportDto = report
        override suspend fun getAllReports(): Map<String, Map<String, de.exit.ridetrackr.data.remote.api.FirebaseReportDto>>? = null
    }

    private val fakeGitHubCloudDbApi = object : de.exit.ridetrackr.data.remote.api.GitHubCloudDbApi {
        override suspend fun getLiveReportsRaw(): Map<String, Map<String, de.exit.ridetrackr.data.remote.api.LiveReportDto>> = emptyMap()
        override suspend fun getFileMetadata(authHeader: String?): de.exit.ridetrackr.data.remote.api.GitHubFileContentDto =
            de.exit.ridetrackr.data.remote.api.GitHubFileContentDto(sha = "dummy")
        override suspend fun updateLiveReports(
            authHeader: String,
            request: de.exit.ridetrackr.data.remote.api.GitHubUpdateContentRequestDto
        ): de.exit.ridetrackr.data.remote.api.GitHubUpdateResponseDto =
            de.exit.ridetrackr.data.remote.api.GitHubUpdateResponseDto()
    }

    @Test
    fun testGetAttractions_returnsTripsdrillDynamicList() = runBlocking {
        val userReportsRepo = UserReportsRepository(fakeUserReportsDao, fakeFirebaseApi, fakeGitHubCloudDbApi)
        val repo = AttractionsRepository(
            themeParksApi = fakeThemeParks,
            wartezeitenApi = fakeWartezeiten,
            plausibilityEngine = PlausibilityEngine(),
            tripsdrillWaitTimeEngine = TripsdrillWaitTimeEngine(),
            userReportsRepository = userReportsRepo
        )

        val attractions = repo.getAttractions("tripsdrill")

        assertTrue("Attractions list for Tripsdrill should not be empty", attractions.isNotEmpty())
        assertTrue(attractions.any { it.name == "Mammut" })
        assertTrue(attractions.any { it.name == "Karacho" })
        assertTrue(attractions.any { it.name == "Hals-über-Kopf" })
        assertTrue(attractions.any { it.name == "Volldampf" })
        assertTrue(attractions.all { it.comparisonNote?.contains("Dynamisch") == true })
    }

    @Test
    fun testGetAttractions_appliesUserReportWhenAvailable() = runBlocking {
        val userReportsRepo = UserReportsRepository(fakeUserReportsDao, fakeFirebaseApi, fakeGitHubCloudDbApi)
        // Add a recent report for Karacho (40 min)
        fakeUserReportsDao.list.add(
            UserWaitTimeReportEntity(
                id = 1L,
                parkId = "tripsdrill",
                attractionId = "tripsdrill-karacho",
                attractionName = "Karacho",
                waitTimeMinutes = 40,
                reportedAtMillis = System.currentTimeMillis() - 60000L // 1 min ago
            )
        )

        val repo = AttractionsRepository(
            themeParksApi = fakeThemeParks,
            wartezeitenApi = fakeWartezeiten,
            plausibilityEngine = PlausibilityEngine(),
            tripsdrillWaitTimeEngine = TripsdrillWaitTimeEngine(),
            userReportsRepository = userReportsRepo
        )

        val attractions = repo.getAttractions("tripsdrill")
        val karacho = attractions.first { it.name == "Karacho" }

        assertEquals(40, karacho.waitTimeMinutes)
        assertTrue(karacho.comparisonNote?.contains("Eigene Vor-Ort-Meldung") == true)
    }
}
