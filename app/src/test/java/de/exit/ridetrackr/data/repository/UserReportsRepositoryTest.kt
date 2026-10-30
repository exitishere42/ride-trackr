package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import de.exit.ridetrackr.data.remote.api.FirebaseRealtimeDbApi
import de.exit.ridetrackr.data.remote.api.FirebaseReportDto
import de.exit.ridetrackr.data.remote.api.GitHubCloudDbApi
import de.exit.ridetrackr.data.remote.api.GitHubFileContentDto
import de.exit.ridetrackr.data.remote.api.GitHubUpdateContentRequestDto
import de.exit.ridetrackr.data.remote.api.GitHubUpdateResponseDto
import de.exit.ridetrackr.data.remote.api.LiveReportDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserReportsRepositoryTest {

    private val fakeDao = object : UserReportsDao {
        val list = mutableListOf<UserWaitTimeReportEntity>()

        override suspend fun insertReport(report: UserWaitTimeReportEntity): Long {
            val assigned = report.copy(id = (list.size + 1).toLong())
            list.add(0, assigned)
            return assigned.id
        }

        override suspend fun getRecentReports(attractionId: String, minTimestamp: Long): List<UserWaitTimeReportEntity> {
            return list.filter { it.attractionId == attractionId && it.reportedAtMillis >= minTimestamp }
        }

        override fun observeLatestReport(attractionId: String): Flow<UserWaitTimeReportEntity?> {
            return flowOf(list.firstOrNull { it.attractionId == attractionId })
        }

        override suspend fun getLatestReport(attractionId: String): UserWaitTimeReportEntity? {
            return list.firstOrNull { it.attractionId == attractionId }
        }

        override suspend fun getRecentParkReports(parkId: String, minTimestamp: Long): List<UserWaitTimeReportEntity> {
            return list.filter { it.parkId == parkId && it.reportedAtMillis >= minTimestamp }
        }

        override suspend fun getAllParkReports(parkId: String): List<UserWaitTimeReportEntity> {
            return list.filter { it.parkId == parkId }
        }

        override fun observeAllReports(): Flow<List<UserWaitTimeReportEntity>> {
            return flowOf(list)
        }

        override suspend fun deleteReport(id: Long) {
            list.removeAll { it.id == id }
        }

        override suspend fun pruneOldReports(olderThanMillis: Long) {
            list.removeAll { it.reportedAtMillis < olderThanMillis }
        }
    }

    private val fakeFirebaseApi = object : FirebaseRealtimeDbApi {
        val cloudReports = mutableMapOf<String, MutableMap<String, FirebaseReportDto>>()

        override suspend fun getParkReports(parkId: String): Map<String, FirebaseReportDto>? {
            return cloudReports[parkId]
        }

        override suspend fun submitReport(
            parkId: String,
            attractionId: String,
            report: FirebaseReportDto
        ): FirebaseReportDto {
            val parkMap = cloudReports.getOrPut(parkId) { mutableMapOf() }
            parkMap[attractionId] = report
            return report
        }

        override suspend fun getAllReports(): Map<String, Map<String, FirebaseReportDto>>? {
            return cloudReports
        }
    }

    private val fakeGitHubCloudDbApi = object : GitHubCloudDbApi {
        val cloudReports = mutableMapOf<String, MutableMap<String, LiveReportDto>>()

        override suspend fun getLiveReportsRaw(): Map<String, Map<String, LiveReportDto>> {
            return cloudReports
        }

        override suspend fun getFileMetadata(authHeader: String?): GitHubFileContentDto {
            return GitHubFileContentDto(sha = "dummy_sha")
        }

        override suspend fun updateLiveReports(
            authHeader: String,
            request: GitHubUpdateContentRequestDto
        ): GitHubUpdateResponseDto {
            return GitHubUpdateResponseDto()
        }
    }

    @Test
    fun testSubmitAndPublishToFirebase() = runBlocking {
        val repo = UserReportsRepository(fakeDao, fakeFirebaseApi, fakeGitHubCloudDbApi)

        val id = repo.submitReport(
            parkId = "tripsdrill",
            attractionId = "tripsdrill-karacho",
            attractionName = "Karacho",
            waitTimeMinutes = 20,
            note = "Sonnig vor Ort"
        )

        assertEquals(1L, id)

        // Check local DB
        val report = repo.getRecentReport("tripsdrill-karacho", maxAgeHours = 1)
        assertNotNull(report)
        assertEquals("Karacho", report?.attractionName)
        assertEquals(20, report?.waitTimeMinutes)

        // Check Firebase online publication
        val onlineReport = fakeFirebaseApi.getParkReports("tripsdrill")?.get("tripsdrill-karacho")
        assertNotNull(onlineReport)
        assertEquals(20, onlineReport?.waitTimeMinutes)
    }

    @Test
    fun testSyncOnlineReportsFromFirebase() = runBlocking {
        val repo = UserReportsRepository(fakeDao, fakeFirebaseApi, fakeGitHubCloudDbApi)

        // Simulate another user published an online wait time for Mammut to Firebase
        val now = System.currentTimeMillis()
        fakeFirebaseApi.submitReport(
            parkId = "tripsdrill",
            attractionId = "tripsdrill-mammut",
            report = FirebaseReportDto(
                parkId = "tripsdrill",
                attractionId = "tripsdrill-mammut",
                attractionName = "Mammut",
                waitTimeMinutes = 35,
                reportedAtMillis = now
            )
        )

        val reports = repo.getRecentParkReports("tripsdrill", maxAgeHours = 1)
        assertTrue(reports.containsKey("tripsdrill-mammut"))
        assertEquals(35, reports["tripsdrill-mammut"]?.waitTimeMinutes)
    }

    @Test
    fun testSyncOnlineReportsFromGitHubCloudDb() = runBlocking {
        val repo = UserReportsRepository(fakeDao, fakeFirebaseApi, fakeGitHubCloudDbApi)

        // Simulate live report in GitHub Cloud DB
        val now = System.currentTimeMillis()
        fakeGitHubCloudDbApi.cloudReports.getOrPut("tripsdrill") { mutableMapOf() }["tripsdrill-karacho"] = LiveReportDto(
            parkId = "tripsdrill",
            attractionId = "tripsdrill-karacho",
            attractionName = "Karacho",
            waitTimeMinutes = 20,
            reportedAtMillis = now,
            note = "Sonnig, normale Abfertigung"
        )

        val reports = repo.getRecentParkReports("tripsdrill", maxAgeHours = 1)
        assertTrue(reports.containsKey("tripsdrill-karacho"))
        assertEquals(20, reports["tripsdrill-karacho"]?.waitTimeMinutes)
        assertEquals("Sonnig, normale Abfertigung", reports["tripsdrill-karacho"]?.note)
    }
}
