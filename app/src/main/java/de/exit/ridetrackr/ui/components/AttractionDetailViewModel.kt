package de.exit.ridetrackr.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import de.exit.ridetrackr.data.model.WikipediaRideData
import de.exit.ridetrackr.data.repository.AttractionsRepository
import de.exit.ridetrackr.data.repository.UserReportsRepository
import de.exit.ridetrackr.data.repository.WikipediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttractionDetailViewModel @Inject constructor(
    private val wikipediaRepository: WikipediaRepository,
    private val userReportsRepository: UserReportsRepository,
    private val attractionsRepository: AttractionsRepository
) : ViewModel() {

    private val _wikiData = MutableStateFlow<WikipediaRideData?>(null)
    val wikiData: StateFlow<WikipediaRideData?> = _wikiData.asStateFlow()

    private val _isLoadingWiki = MutableStateFlow(false)
    val isLoadingWiki: StateFlow<Boolean> = _isLoadingWiki.asStateFlow()

    private val _latestUserReport = MutableStateFlow<UserWaitTimeReportEntity?>(null)
    val latestUserReport: StateFlow<UserWaitTimeReportEntity?> = _latestUserReport.asStateFlow()

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    fun loadData(attractionId: String, attractionName: String, parkName: String?) {
        loadWikipedia(attractionName, parkName)
        loadLatestReport(attractionId)
    }

    private fun loadWikipedia(attractionName: String, parkName: String?) {
        viewModelScope.launch {
            _isLoadingWiki.value = true
            try {
                val data = wikipediaRepository.getRideData(attractionName, parkName)
                _wikiData.value = data
            } catch (_: Exception) {
                _wikiData.value = null
            } finally {
                _isLoadingWiki.value = false
            }
        }
    }

    private fun loadLatestReport(attractionId: String) {
        viewModelScope.launch {
            val report = userReportsRepository.getRecentReport(attractionId, maxAgeHours = 4)
            _latestUserReport.value = report
        }
    }

    fun submitWaitTime(
        parkId: String,
        attractionId: String,
        attractionName: String,
        minutes: Int,
        note: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingReport.value = true
            try {
                userReportsRepository.submitReport(
                    parkId = parkId,
                    attractionId = attractionId,
                    attractionName = attractionName,
                    waitTimeMinutes = minutes,
                    note = note
                )
                attractionsRepository.invalidateCache(parkId)
                loadLatestReport(attractionId)
                onSuccess()
            } catch (_: Exception) {
                // Ignore DB error
            } finally {
                _isSubmittingReport.value = false
            }
        }
    }
}
