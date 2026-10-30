package de.exit.ridetrackr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import de.exit.ridetrackr.data.local.dao.FavoritesDao
import de.exit.ridetrackr.data.local.dao.UserReportsDao
import de.exit.ridetrackr.data.local.entity.FavoriteAttractionEntity
import de.exit.ridetrackr.data.local.entity.FavoriteParkEntity
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity

@Database(
    entities = [
        FavoriteAttractionEntity::class,
        FavoriteParkEntity::class,
        UserWaitTimeReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun userReportsDao(): UserReportsDao
}
