package de.exit.ridetrackr.ui.screens.parks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.exit.ridetrackr.data.model.Park
import de.exit.ridetrackr.data.repository.ParksRepository
import de.exit.ridetrackr.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParksUiState(
    val parks: List<Park> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ParksViewModel @Inject constructor(
    private val parksRepository: ParksRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _rawParks = MutableStateFlow<List<Park>>(emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ParksUiState> = combine(
        _rawParks,
        _searchQuery,
        _isLoading,
        _errorMessage
    ) { parks, query, loading, error ->
        val filtered = if (query.isBlank()) {
            parks
        } else {
            parks.filter { it.name.contains(query, ignoreCase = true) }
        }
        ParksUiState(
            parks = filtered,
            searchQuery = query,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ParksUiState(isLoading = true)
    )

    init {
        loadParks()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun loadParks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = parksRepository.getParks()
                _rawParks.value = list
            } catch (e: Exception) {
                _errorMessage.value = "Konnte Parks nicht laden: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPark(parkId: String, parkName: String) {
        viewModelScope.launch {
            settingsRepository.saveSelectedPark(parkId, parkName)
        }
    }
}
