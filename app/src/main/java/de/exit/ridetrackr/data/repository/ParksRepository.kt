package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.model.Park
import de.exit.ridetrackr.data.remote.api.ThemeParksWikiApi
import de.exit.ridetrackr.data.remote.api.WartezeitenApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParksRepository @Inject constructor(
    private val themeParksApi: ThemeParksWikiApi,
    private val wartezeitenApi: WartezeitenApi
) {
    @Volatile
    private var memoryCache: List<Park>? = null

    suspend fun getParks(forceRefresh: Boolean = false): List<Park> = withContext(Dispatchers.IO) {
        val cached = memoryCache
        if (!forceRefresh && cached != null && cached.isNotEmpty()) {
            return@withContext cached
        }

        val parksMap = mutableMapOf<String, Park>()

        // 1. Fetch from ThemeParks.wiki
        try {
            val response = themeParksApi.getDestinations()
            for (dest in response.destinations) {
                for (p in dest.parks) {
                    val isTripsdrill = p.name.contains("Tripsdrill", ignoreCase = true) || dest.name.contains("Tripsdrill", ignoreCase = true)
                    parksMap[p.id] = Park(
                        id = p.id,
                        name = if (isTripsdrill) "Erlebnispark Tripsdrill" else p.name,
                        source = DataSource.THEMEPARKS_WIKI,
                        isOpen = true
                    )
                }
            }
        } catch (_: Exception) {
            // ThemeParks.wiki error fallback
        }

        // Always ensure Erlebnispark Tripsdrill is present even if destinations list fluctuates
        val hasTripsdrill = parksMap.values.any { it.name.contains("Tripsdrill", ignoreCase = true) }
        if (!hasTripsdrill) {
            parksMap["tripsdrill"] = Park(
                id = "tripsdrill",
                name = "Erlebnispark Tripsdrill",
                source = DataSource.THEMEPARKS_WIKI,
                isOpen = true
            )
        }

        // 2. Fetch from Wartezeiten.APP
        try {
            val wzParks = wartezeitenApi.getParks()
            for (wz in wzParks) {
                val existing = parksMap.values.firstOrNull { 
                    it.name.contains(wz.name, ignoreCase = true) || wz.name.contains(it.name, ignoreCase = true)
                }
                if (existing != null) {
                    parksMap[existing.id] = existing.copy(source = DataSource.CROSS_VERIFIED)
                } else {
                    parksMap[wz.id] = Park(
                        id = wz.id,
                        name = wz.name,
                        source = DataSource.WARTEZEITEN_APP,
                        isOpen = wz.isOpen ?: true
                    )
                }
            }
        } catch (_: Exception) {
            // Wartezeiten.APP fallback
        }

        val result = parksMap.values.sortedWith(
            compareByDescending<Park> { it.name.contains("Tripsdrill") || it.name.contains("Europa-Park") || it.name.contains("Phantasialand") }
                .thenBy { it.name }
        )
        memoryCache = result
        result
    }
}
