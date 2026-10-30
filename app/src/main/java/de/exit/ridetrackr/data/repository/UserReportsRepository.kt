package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserReportsRepository @Inject constructor(
    private val userReportsDao: UserReportsDao
) {
    val allReports: Flow<List<UserWaitTimeReportEntity>> = userReportsDao.observeAllReports()

    fun observeLatestReport(attractionId: String): Flow<UserWaitTimeReportEntity?> {
        return userReportsDao.observeLatestReport(attractionId)
    }

    suspend fun getRecentReport(attractionId: String, maxAgeHours: Int = 2): UserWaitTimeReportEntity? {
        val minTimestamp = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000L)
        return userReportsDao.getRecentReports(attractionId, minTimestamp).firstOrNull()
    }

    suspend fun getRecentParkReports(parkId: String, maxAgeHours: Int = 2): Map<String, UserWaitTimeReportEntity> {
        val minTimestamp = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000L)
        val reports = userReportsDao.getRecentParkReports(parkId, minTimestamp)
        return reports.groupBy { it.attractionId }.mapValues { it.value.first() }
    }

    suspend fun submitReport(
        parkId: String,
        attractionId: String,
        attractionName: String,
        waitTimeMinutes: Int,
        note: String? = null
    ): Long {
        val report = UserWaitTimeReportEntity(
            parkId = parkId,
            attractionId = attractionId,
            attractionName = attractionName,
            waitTimeMinutes = waitTimeMinutes,
            reportedAtMillis = System.currentTimeMillis(),
            note = note
        )
        return userReportsDao.insertReport(report)
    }

    suspend fun deleteReport(id: Long) {
        userReportsDao.deleteReport(id)
    }
}
