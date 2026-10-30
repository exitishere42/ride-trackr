package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun testSubmitAndRetrieveRecentReport() = runBlocking {
        val repo = UserReportsRepository(fakeDao)

        val id = repo.submitReport(
            parkId = "tripsdrill",
            attractionId = "tripsdrill-karacho",
            attractionName = "Karacho",
            waitTimeMinutes = 20,
            note = "Sonnig vor Ort"
        )

        assertEquals(1L, id)

        val report = repo.getRecentReport("tripsdrill-karacho", maxAgeHours = 1)
        assertNotNull(report)
        assertEquals("Karacho", report?.attractionName)
        assertEquals(20, report?.waitTimeMinutes)

        val parkReports = repo.getRecentParkReports("tripsdrill", maxAgeHours = 1)
        assertEquals(1, parkReports.size)
        assertEquals(20, parkReports["tripsdrill-karacho"]?.waitTimeMinutes)
    }

    @Test
    fun testOlderReportIgnored() = runBlocking {
        val repo = UserReportsRepository(fakeDao)
        val oldTimestamp = System.currentTimeMillis() - (5 * 60 * 60 * 1000L) // 5 hours ago

        fakeDao.insertReport(
            UserWaitTimeReportEntity(
                parkId = "tripsdrill",
                attractionId = "tripsdrill-mammut",
                attractionName = "Mammut",
                waitTimeMinutes = 35,
                reportedAtMillis = oldTimestamp
            )
        )

        // When asking for recent report within 2 hours, should be null
        val recent = repo.getRecentReport("tripsdrill-mammut", maxAgeHours = 2)
        assertNull(recent)
    }
}
