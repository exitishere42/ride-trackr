package de.exit.ridetrackr.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.exit.ridetrackr.data.model.Park
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.data.repository.AttractionsRepository
import de.exit.ridetrackr.data.repository.FavoritesRepository
import de.exit.ridetrackr.data.repository.ParksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchCategory {
    ALL,
    PARKS,
    ATTRACTIONS
}

data class SearchUiState(
    val query: String = "",
    val category: SearchCategory = SearchCategory.ALL,
    val parks: List<Park> = emptyList(),
    val attractions: List<ResolvedAttraction> = emptyList(),
    val favoriteParkIds: Set<String> = emptySet(),
    val favoriteAttractionIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val parksRepository: ParksRepository,
    private val attractionsRepository: AttractionsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _category = MutableStateFlow(SearchCategory.ALL)
    private val _allParks = MutableStateFlow<List<Park>>(emptyList())
    private val _cachedAttractions = MutableStateFlow<List<ResolvedAttraction>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val searchFilterFlow = combine(_query, _category) { q, cat ->
        Pair(q, cat)
    }

    private val searchDataFlow = combine(_allParks, _cachedAttractions) { parks, attractions ->
        Pair(parks, attractions)
    }

    private val favoritesFlow = combine(
        favoritesRepository.favoriteParks,
        favoritesRepository.favorites
    ) { favParks, favAttractions ->
        Pair(favParks.map { it.id }.toSet(), favAttractions.map { it.id }.toSet())
    }

    val uiState: StateFlow<SearchUiState> = combine(
        searchFilterFlow,
        searchDataFlow,
        favoritesFlow,
        _isLoading
    ) { (query, category), (parks, attractions), (favParkIds, favAttractionIds), loading ->
        val filteredParks = if (category == SearchCategory.ATTRACTIONS) {
            emptyList()
        } else if (query.isBlank()) {
            parks
        } else {
            parks.filter { it.name.contains(query, ignoreCase = true) }
        }

        val filteredAttractions = if (category == SearchCategory.PARKS) {
            emptyList()
        } else if (query.isBlank()) {
            attractions.take(15)
        } else {
            attractions.filter { it.name.contains(query, ignoreCase = true) }
        }

        SearchUiState(
            query = query,
            category = category,
            parks = filteredParks,
            attractions = filteredAttractions,
            favoriteParkIds = favParkIds,
            favoriteAttractionIds = favAttractionIds,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(isLoading = true)
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val parks = parksRepository.getParks()
                _allParks.value = parks
                _isLoading.value = false

                // Pre-cache popular parks' attractions in background without blocking UI
                val popularIds = listOf(
                    "tripsdrill",
                    "639738d3-9574-4f60-ab5b-4c392901320b",
                    "phantasialand"
                )
                val preloaded = mutableListOf<ResolvedAttraction>()
                for (id in popularIds) {
                    try {
                        preloaded.addAll(attractionsRepository.getAttractions(id))
                    } catch (_: Exception) {}
                }
                _cachedAttractions.value = preloaded
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                _isLoading.value = false
            }
        }
    }

    fun onQueryChanged(q: String) {
        _query.value = q
    }

    fun setCategory(cat: SearchCategory) {
        _category.value = cat
    }

    fun toggleFavoritePark(park: Park) {
        viewModelScope.launch {
            favoritesRepository.toggleFavoritePark(park)
        }
    }

    fun toggleFavoriteAttraction(attraction: ResolvedAttraction) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(attraction, attraction.parkId)
        }
    }
}
