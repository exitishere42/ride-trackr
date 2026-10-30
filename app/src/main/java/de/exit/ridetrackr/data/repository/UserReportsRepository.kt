package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import de.exit.ridetrackr.data.remote.api.FirebaseRealtimeDbApi
import de.exit.ridetrackr.data.remote.api.FirebaseReportDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserReportsRepository @Inject constructor(
    private val userReportsDao: UserReportsDao,
    private val firebaseApi: FirebaseRealtimeDbApi
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

        // 1. Sync online reports from Firebase Cloud Database
        try {
            val onlineMap = firebaseApi.getParkReports(parkId)
            if (onlineMap != null) {
                for ((_, dto) in onlineMap) {
                    if (dto.reportedAtMillis >= minTimestamp) {
                        userReportsDao.insertReport(
                            UserWaitTimeReportEntity(
                                parkId = dto.parkId,
                                attractionId = dto.attractionId,
                                attractionName = dto.attractionName,
                                waitTimeMinutes = dto.waitTimeMinutes,
                                reportedAtMillis = dto.reportedAtMillis,
                                note = dto.note
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Offline or network error: continue with cached data
        }

        // 2. Return combined reports
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
        val now = System.currentTimeMillis()
        val report = UserWaitTimeReportEntity(
            parkId = parkId,
            attractionId = attractionId,
            attractionName = attractionName,
            waitTimeMinutes = waitTimeMinutes,
            reportedAtMillis = now,
            note = note
        )

        // 1. Save in local cache
        val localId = userReportsDao.insertReport(report)

        // 2. Publish to Firebase Realtime Cloud Database online
        try {
            firebaseApi.submitReport(
                parkId = parkId,
                attractionId = attractionId,
                report = FirebaseReportDto(
                    parkId = parkId,
                    attractionId = attractionId,
                    attractionName = attractionName,
                    waitTimeMinutes = waitTimeMinutes,
                    reportedAtMillis = now,
                    note = note
                )
            )
        } catch (_: Exception) {
            // Keep local report if offline
        }

        return localId
    }

    suspend fun deleteReport(id: Long) {
        userReportsDao.deleteReport(id)
    }
}
