package de.exit.ridetrackr.ui.screens.attractions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.exit.ridetrackr.data.model.AttractionStatus
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

enum class FilterMode {
    ALL,
    OPEN_ONLY,
    SHORT_WAIT_ONLY
}

enum class SortMode {
    WAIT_TIME,
    NAME
}

private data class DisplayOptions(
    val filterMode: FilterMode = FilterMode.ALL,
    val sortMode: SortMode = SortMode.WAIT_TIME
)

private data class StatusInfo(
    val parkName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class AttractionsUiState(
    val parkName: String = "",
    val attractions: List<ResolvedAttraction> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val filterMode: FilterMode = FilterMode.ALL,
    val sortMode: SortMode = SortMode.WAIT_TIME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AttractionsViewModel @Inject constructor(
    private val attractionsRepository: AttractionsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _parkId = MutableStateFlow("")
    private val _parkName = MutableStateFlow("")
    private val _rawAttractions = MutableStateFlow<List<ResolvedAttraction>>(emptyList())
    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    private val _sortMode = MutableStateFlow(SortMode.WAIT_TIME)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val displayOptionsFlow = combine(_filterMode, _sortMode) { filter, sort ->
        DisplayOptions(filter, sort)
    }

    private val statusInfoFlow = combine(_parkName, _isLoading, _errorMessage) { name, loading, error ->
        StatusInfo(name, loading, error)
    }

    val uiState: StateFlow<AttractionsUiState> = combine(
        _rawAttractions,
        favoritesRepository.favorites,
        displayOptionsFlow,
        statusInfoFlow
    ) { raw, favs, options, status ->
        val favIds = favs.map { it.id }.toSet()

        val filtered = raw.filter { item ->
            when (options.filterMode) {
                FilterMode.ALL -> true
                FilterMode.OPEN_ONLY -> item.status == AttractionStatus.OPERATING
                FilterMode.SHORT_WAIT_ONLY -> item.status == AttractionStatus.OPERATING && (item.waitTimeMinutes ?: 999) <= 20
            }
        }

        val sorted = when (options.sortMode) {
            SortMode.WAIT_TIME -> filtered.sortedWith(
                compareBy(
                    { it.status != AttractionStatus.OPERATING },
                    { it.waitTimeMinutes ?: 999 },
                    { it.name }
                )
            )
            SortMode.NAME -> filtered.sortedBy { it.name }
        }

        AttractionsUiState(
            parkName = status.parkName,
            attractions = sorted,
            favoriteIds = favIds,
            filterMode = options.filterMode,
            sortMode = options.sortMode,
            isLoading = status.isLoading,
            errorMessage = status.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttractionsUiState(isLoading = true)
    )

    fun initialize(parkId: String, parkName: String) {
        if (_parkId.value == parkId && _rawAttractions.value.isNotEmpty()) return
        _parkId.value = parkId
        _parkName.value = parkName
        loadAttractions()
    }

    fun loadAttractions() {
        val currentId = _parkId.value
        if (currentId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = attractionsRepository.getAttractions(currentId)
                _rawAttractions.value = list
            } catch (e: Exception) {
                _errorMessage.value = "Konnte Wartezeiten nicht laden: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun toggleFavorite(attraction: ResolvedAttraction) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(attraction, _parkName.value)
        }
    }
}
