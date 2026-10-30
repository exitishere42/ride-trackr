package de.exit.ridetrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserReportsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: UserWaitTimeReportEntity): Long

    @Query("SELECT * FROM user_wait_time_reports WHERE attractionId = :attractionId AND reportedAtMillis >= :minTimestamp ORDER BY reportedAtMillis DESC")
    suspend fun getRecentReports(attractionId: String, minTimestamp: Long): List<UserWaitTimeReportEntity>

    @Query("SELECT * FROM user_wait_time_reports WHERE attractionId = :attractionId ORDER BY reportedAtMillis DESC LIMIT 1")
    fun observeLatestReport(attractionId: String): Flow<UserWaitTimeReportEntity?>

    @Query("SELECT * FROM user_wait_time_reports WHERE attractionId = :attractionId ORDER BY reportedAtMillis DESC LIMIT 1")
    suspend fun getLatestReport(attractionId: String): UserWaitTimeReportEntity?

    @Query("SELECT * FROM user_wait_time_reports WHERE parkId = :parkId AND reportedAtMillis >= :minTimestamp ORDER BY reportedAtMillis DESC")
    suspend fun getRecentParkReports(parkId: String, minTimestamp: Long): List<UserWaitTimeReportEntity>

    @Query("SELECT * FROM user_wait_time_reports WHERE parkId = :parkId ORDER BY reportedAtMillis DESC")
    suspend fun getAllParkReports(parkId: String): List<UserWaitTimeReportEntity>

    @Query("SELECT * FROM user_wait_time_reports ORDER BY reportedAtMillis DESC")
    fun observeAllReports(): Flow<List<UserWaitTimeReportEntity>>

    @Query("DELETE FROM user_wait_time_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)

    @Query("DELETE FROM user_wait_time_reports WHERE reportedAtMillis < :olderThanMillis")
    suspend fun pruneOldReports(olderThanMillis: Long)
}
