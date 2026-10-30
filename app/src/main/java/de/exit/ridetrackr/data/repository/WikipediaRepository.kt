package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.model.WikipediaRideData
import de.exit.ridetrackr.data.remote.api.WikipediaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WikipediaRepository @Inject constructor(
    private val wikipediaApi: WikipediaApi
) {
    private val cache = ConcurrentHashMap<String, WikipediaRideData>()

    suspend fun getRideData(attractionName: String, parkName: String? = null): WikipediaRideData? = withContext(Dispatchers.IO) {
        val cacheKey = "$attractionName::$parkName".lowercase().trim()
        cache[cacheKey]?.let { return@withContext it }

        try {
            // 1. Try finding Wikipedia article title
            val cleanAttraction = attractionName
                .replace(Regex("\\s*\\(.*?\\)"), "") // remove parenthetical notes
                .trim()

            val searchQueries = listOfNotNull(
                if (!parkName.isNullOrBlank()) "$cleanAttraction $parkName" else null,
                "$cleanAttraction Achterbahn",
                cleanAttraction
            )

            var matchedTitle: String? = null
            for (query in searchQueries) {
                val searchRes = wikipediaApi.search(query)
                val items = searchRes.query?.search ?: emptyList()
                if (items.isNotEmpty()) {
                    // Try to find the most relevant title
                    val best = items.firstOrNull { item ->
                        val lower = item.title.lowercase()
                        val attrLower = cleanAttraction.lowercase()
                        (lower.contains(attrLower) || attrLower.contains(lower)) &&
                                !lower.contains("liste") && !lower.contains("kategorie")
                    } ?: items.firstOrNull { !it.title.lowercase().startsWith("liste") }

                    if (best != null) {
                        matchedTitle = best.title
                        break
                    }
                }
            }

            if (matchedTitle == null) {
                return@withContext null
            }

            // 2. Fetch REST Summary
            val encodedTitle = URLEncoder.encode(matchedTitle.replace(" ", "_"), StandardCharsets.UTF_8.name())
            val summary = try {
                wikipediaApi.getSummary(encodedTitle)
            } catch (_: Exception) {
                null
            }

            // 3. Fetch Wikitext Infobox for technical specifications
            var manufacturer: String? = null
            var speed: Int? = null
            var height: Int? = null
            var length: Int? = null
            var inversions: Int? = null
            var openingDate: String? = null
            var model: String? = null

            try {
                val contentRes = wikipediaApi.getArticleContent(matchedTitle)
                val pages = contentRes.query?.pages ?: emptyMap()
                val page = pages.values.firstOrNull()
                val wikitext = page?.revisions?.firstOrNull()?.slots?.main?.content

                if (wikitext != null) {
                    val lines = wikitext.lines()
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("|") && trimmed.contains("=")) {
                            val parts = trimmed.substring(1).split("=", limit = 2)
                            val key = parts[0].trim().uppercase()
                            val value = cleanWikiValue(parts[1])

                            when {
                                key.contains("HERSTELLER") || key.contains("MANUFACTURER") -> {
                                    if (manufacturer == null && value.isNotBlank()) manufacturer = value
                                }
                                key.contains("GESCHWINDIGKEIT") || key.contains("SPEED") -> {
                                    if (speed == null) speed = extractNumber(value)
                                }
                                key.contains("HÖHE") || key.contains("HOEHE") || key.contains("HEIGHT") -> {
                                    if (height == null) height = extractNumber(value)
                                }
                                key.contains("LÄNGE") || key.contains("LAENGE") || key.contains("LENGTH") -> {
                                    if (length == null) length = extractNumber(value)
                                }
                                key.contains("INVERSIONEN") || key.contains("INVERSIONS") -> {
                                    if (inversions == null) inversions = extractNumber(value)
                                }
                                key.contains("ERÖFFNUNG") || key.contains("EROEFFNUNG") || key.contains("OPENED") -> {
                                    if (openingDate == null && value.isNotBlank()) openingDate = value
                                }
                                key.contains("MODELL") || key.contains("TYP") -> {
                                    if (model == null && value.isNotBlank()) model = value
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Infobox parsing optional
            }

            val imageUrl = summary?.thumbnail?.source ?: summary?.originalimage?.source
            val description = summary?.extract ?: summary?.description
            val pageUrl = summary?.contentUrls?.desktop?.page

            val result = WikipediaRideData(
                title = summary?.title ?: matchedTitle,
                description = description,
                imageUrl = imageUrl,
                pageUrl = pageUrl,
                manufacturer = manufacturer,
                topSpeedKmh = speed,
                heightMeters = height,
                trackLengthMeters = length,
                inversions = inversions,
                openingDate = openingDate,
                model = model
            )

            cache[cacheKey] = result
            result
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanWikiValue(value: String): String {
        return value
            .replace(Regex("<ref.*?</ref>"), "")
            .replace(Regex("<ref.*?>"), "")
            .replace(Regex("\\[\\[(?:[^|\\]]*\\|)?([^\\]]+)\\]\\]"), "$1") // [[Target|Text]] -> Text
            .replace(Regex("\\{\\{.*?\\}\\}"), "")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun extractNumber(value: String): Int? {
        val match = Regex("(\\d+([.,]\\d+)?)").find(value) ?: return null
        val numStr = match.groupValues[1].replace(",", ".")
        return numStr.toDoubleOrNull()?.toInt()
    }
}
