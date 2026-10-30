package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.engine.PlausibilityEngine
import de.exit.ridetrackr.data.engine.TripsdrillWaitTimeEngine
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.data.remote.api.LiveItemDto
import de.exit.ridetrackr.data.remote.api.ThemeParksWikiApi
import de.exit.ridetrackr.data.remote.api.WartezeitenApi
import de.exit.ridetrackr.data.remote.api.WartezeitenAttractionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttractionsRepository @Inject constructor(
    private val themeParksApi: ThemeParksWikiApi,
    private val wartezeitenApi: WartezeitenApi,
    private val plausibilityEngine: PlausibilityEngine,
    private val tripsdrillWaitTimeEngine: TripsdrillWaitTimeEngine,
    private val userReportsRepository: UserReportsRepository
) {
    private val cache = ConcurrentHashMap<String, Pair<Long, List<ResolvedAttraction>>>()
    private val cacheTtlMillis = 60_000L // 1 Minute Cache

    fun invalidateCache(parkId: String) {
        cache.remove(parkId)
    }

    suspend fun getAttractions(parkId: String, forceRefresh: Boolean = false): List<ResolvedAttraction> = withContext(Dispatchers.IO) {
        val cached = cache[parkId]
        if (!forceRefresh && cached != null && (System.currentTimeMillis() - cached.first) < cacheTtlMillis) {
            return@withContext cached.second
        }

        var tpItems = emptyList<LiveItemDto>()
        var wzItems = emptyList<WartezeitenAttractionDto>()

        // 1. Fetch from ThemeParks.wiki
        try {
            val response = themeParksApi.getLiveWaitTimes(parkId)
            tpItems = response.liveData
        } catch (_: Exception) {
            // Error handling or single-source fallback
        }

        // 2. Fetch from Wartezeiten.APP
        try {
            wzItems = wartezeitenApi.getWaitingTimes(parkId)
        } catch (_: Exception) {
            // Error handling or single-source fallback
        }

        // 3. Resolve & Compare using PlausibilityEngine
        var resolved = plausibilityEngine.resolve(
            parkId = parkId,
            themeParksItems = tpItems,
            wartezeitenItems = wzItems
        )

        // Robust dynamic fallback for Parks without active live API feed (e.g. Erlebnispark Tripsdrill)
        if (resolved.isEmpty() && parkId.contains("tripsdrill", ignoreCase = true)) {
            // Fetch live regional crowd level from online data (Baden-Württemberg index)
            val onlineCrowd = try {
                wartezeitenApi.getCrowdLevel("europapark").crowdLevel
            } catch (_: Exception) {
                null
            }

            // Ingest database reports as empirical training data
            val trainingReports = try {
                userReportsRepository.getAllParkReports(parkId)
            } catch (_: Exception) {
                emptyList()
            }

            resolved = tripsdrillWaitTimeEngine.generateAttractions(
                parkId = parkId,
                onlineCrowdLevel = onlineCrowd,
                trainingReports = trainingReports
            )
        }

        // 4. Blend in recent user reports from the database (on-site reports override/calibrate)
        try {
            val recentReports = userReportsRepository.getRecentParkReports(parkId, maxAgeHours = 3)
            if (recentReports.isNotEmpty()) {
                val now = System.currentTimeMillis()
                resolved = resolved.map { attr ->
                    val report = recentReports[attr.id] ?: recentReports.values.firstOrNull {
                        it.attractionName.equals(attr.name, ignoreCase = true)
                    }
                    if (report != null) {
                        val minutesAgo = ((now - report.reportedAtMillis) / 60000L).coerceAtLeast(0)
                        val agoText = if (minutesAgo < 1) "gerade eben" else "vor $minutesAgo Min"
                        val timeFormatted = java.text.SimpleDateFormat("HH:mm", java.util.Locale.GERMANY)
                            .format(java.util.Date(report.reportedAtMillis))
                        attr.copy(
                            waitTimeMinutes = report.waitTimeMinutes,
                            isCrossVerified = true,
                            comparisonNote = "Eigene Vor-Ort-Meldung ($timeFormatted Uhr • $agoText): ${report.waitTimeMinutes} min",
                            lastUpdatedMillis = report.reportedAtMillis
                        )
                    } else {
                        attr
                    }
                }
            }
        } catch (_: Exception) {
            // Room DB fallback
        }

        cache[parkId] = Pair(System.currentTimeMillis(), resolved)
        resolved
    }
}
