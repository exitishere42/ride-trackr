package de.exit.ridetrackr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_attractions")
data class FavoriteAttractionEntity(
    @PrimaryKey
    val id: String,
    val parkId: String,
    val parkName: String,
    val name: String,
    val lastKnownWaitTime: Int = 0,
    val isAlertEnabled: Boolean = true,
    val maxAlertWaitTime: Int = 30,
    val addedAt: Long = System.currentTimeMillis()
)
