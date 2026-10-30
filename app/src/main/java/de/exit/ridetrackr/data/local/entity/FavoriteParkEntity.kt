package de.exit.ridetrackr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_parks")
data class FavoriteParkEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)
