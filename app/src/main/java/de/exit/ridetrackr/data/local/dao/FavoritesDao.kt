package de.exit.ridetrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.exit.ridetrackr.data.local.entity.FavoriteAttractionEntity
import de.exit.ridetrackr.data.local.entity.FavoriteParkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    // Attractions
    @Query("SELECT * FROM favorite_attractions ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteAttractionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(attraction: FavoriteAttractionEntity)

    @Query("DELETE FROM favorite_attractions WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_attractions WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    // Parks
    @Query("SELECT * FROM favorite_parks ORDER BY addedAt DESC")
    fun getAllFavoriteParks(): Flow<List<FavoriteParkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoritePark(park: FavoriteParkEntity)

    @Query("DELETE FROM favorite_parks WHERE id = :id")
    suspend fun deleteFavoritePark(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_parks WHERE id = :id)")
    fun isFavoritePark(id: String): Flow<Boolean>
}
