package de.exit.ridetrackr.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.exit.ridetrackr.data.local.entity.FavoriteAttractionEntity
import de.exit.ridetrackr.data.local.entity.FavoriteParkEntity
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.data.repository.AttractionsRepository
import de.exit.ridetrackr.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val favoriteParks: List<FavoriteParkEntity> = emptyList(),
    val favoriteAttractions: List<FavoriteAttractionEntity> = emptyList(),
    val liveAttractionData: Map<String, ResolvedAttraction> = emptyMap(),
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val attractionsRepository: AttractionsRepository
) : ViewModel() {

    private val _liveAttractions = MutableStateFlow<Map<String, ResolvedAttraction>>(emptyMap())
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        favoritesRepository.favoriteParks,
        favoritesRepository.favorites,
        _liveAttractions,
        _isRefreshing
    ) { parks, attractions, liveMap, refreshing ->
        HomeUiState(
            favoriteParks = parks,
            favoriteAttractions = attractions,
            liveAttractionData = liveMap,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            favoritesRepository.favorites.collect { favs ->
                refreshLiveWaitTimes(favs)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val favs = uiState.value.favoriteAttractions
                refreshLiveWaitTimes(favs)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun refreshLiveWaitTimes(favs: List<FavoriteAttractionEntity>) {
        if (favs.isEmpty()) return
        val map = mutableMapOf<String, ResolvedAttraction>()
        val byPark = favs.groupBy { it.parkId }
        for ((parkId, list) in byPark) {
            try {
                val liveList = attractionsRepository.getAttractions(parkId)
                for (fav in list) {
                    val matching = liveList.firstOrNull { it.id == fav.id }
                    if (matching != null) {
                        map[fav.id] = matching
                    }
                }
            } catch (_: Exception) {
                // Ignore transient network errors
            }
        }
        _liveAttractions.value = map
    }

    fun removeFavoriteAttraction(id: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(id)
        }
    }

    fun removeFavoritePark(id: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavoritePark(id)
        }
    }
}
