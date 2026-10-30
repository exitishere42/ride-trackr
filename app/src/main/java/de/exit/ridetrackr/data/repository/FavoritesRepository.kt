package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.local.dao.FavoritesDao
import de.exit.ridetrackr.data.local.entity.FavoriteAttractionEntity
import de.exit.ridetrackr.data.local.entity.FavoriteParkEntity
import de.exit.ridetrackr.data.model.Park
import de.exit.ridetrackr.data.model.ResolvedAttraction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao
) {
    val favorites: Flow<List<FavoriteAttractionEntity>> = favoritesDao.getAllFavorites()
    val favoriteParks: Flow<List<FavoriteParkEntity>> = favoritesDao.getAllFavoriteParks()

    fun isFavorite(id: String): Flow<Boolean> = favoritesDao.isFavorite(id)
    fun isFavoritePark(id: String): Flow<Boolean> = favoritesDao.isFavoritePark(id)

    suspend fun toggleFavorite(attraction: ResolvedAttraction, parkName: String) = withContext(Dispatchers.IO) {
        val currentlyFav = favoritesDao.isFavorite(attraction.id).first()
        if (currentlyFav) {
            favoritesDao.deleteFavorite(attraction.id)
        } else {
            favoritesDao.insertFavorite(
                FavoriteAttractionEntity(
                    id = attraction.id,
                    parkId = attraction.parkId,
                    parkName = parkName,
                    name = attraction.name,
                    lastKnownWaitTime = attraction.waitTimeMinutes ?: 0,
                    isAlertEnabled = true
                )
            )
        }
    }

    suspend fun toggleFavoritePark(park: Park) = withContext(Dispatchers.IO) {
        val currentlyFav = favoritesDao.isFavoritePark(park.id).first()
        if (currentlyFav) {
            favoritesDao.deleteFavoritePark(park.id)
        } else {
            favoritesDao.insertFavoritePark(
                FavoriteParkEntity(
                    id = park.id,
                    name = park.name
                )
            )
        }
    }

    suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO) {
        favoritesDao.deleteFavorite(id)
    }

    suspend fun removeFavoritePark(id: String) = withContext(Dispatchers.IO) {
        favoritesDao.deleteFavoritePark(id)
    }
}
