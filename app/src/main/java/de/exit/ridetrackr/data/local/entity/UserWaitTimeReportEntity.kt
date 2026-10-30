package de.exit.ridetrackr.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_wait_time_reports",
    indices = [
        Index(value = ["attractionId"]),
        Index(value = ["parkId"]),
        Index(value = ["reportedAtMillis"])
    ]
)
data class UserWaitTimeReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parkId: String,
    val attractionId: String,
    val attractionName: String,
    val waitTimeMinutes: Int,
    val reportedAtMillis: Long = System.currentTimeMillis(),
    val note: String? = null
)
