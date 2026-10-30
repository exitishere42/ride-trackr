package de.exit.ridetrackr.data.engine

import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.model.ResolvedAttraction
import de.exit.ridetrackr.data.remote.api.LiveItemDto
import de.exit.ridetrackr.data.remote.api.WartezeitenAttractionDto
import java.time.Instant
import kotlin.math.abs

class PlausibilityEngine {

    /**
     * Resolves and compares attraction data from both APIs.
     * Determines which data point is more accurate based on timestamp freshness,
     * status consensus, and disruption priority.
     */
    fun resolve(
        parkId: String,
        themeParksItems: List<LiveItemDto>,
        wartezeitenItems: List<WartezeitenAttractionDto>
    ): List<ResolvedAttraction> {
        val resolvedList = mutableListOf<ResolvedAttraction>()
        val matchedWartezeiten = mutableSetOf<String>()

        for (tpItem in themeParksItems) {
            // Only process attractions (exclude shows/restaurants if typed)
            if (tpItem.entityType != null && tpItem.entityType != "ATTRACTION") continue

            val normalizedTpName = normalize(tpItem.name)
            val matchingWartezeit = wartezeitenItems.firstOrNull { wz ->
                !matchedWartezeiten.contains(wz.resolveId()) && areNamesMatching(normalizedTpName, normalize(wz.name))
            }

            if (matchingWartezeit != null) {
                matchedWartezeiten.add(matchingWartezeit.resolveId())
                resolvedList.add(resolveBoth(parkId, tpItem, matchingWartezeit))
            } else {
                // Exclusive to ThemeParks.wiki (e.g. Tripsdrill or extra rides)
                resolvedList.add(resolveThemeParksOnly(parkId, tpItem))
            }
        }

        // Add any remaining Wartezeiten.APP items not matched
        for (wzItem in wartezeitenItems) {
            if (!matchedWartezeiten.contains(wzItem.resolveId())) {
                resolvedList.add(resolveWartezeitenOnly(parkId, wzItem))
            }
        }

        return resolvedList.sortedBy { it.name }
    }

    private fun resolveBoth(
        parkId: String,
        tp: LiveItemDto,
        wz: WartezeitenAttractionDto
    ): ResolvedAttraction {
        val tpWait = tp.queue?.STANDBY?.waitTime
        val wzWait = wz.waitTime

        val tpTime = parseIsoToMillis(tp.lastUpdated)
        val wzTime = parseIsoToMillis(wz.lastUpdated)

        val tpStatus = mapStatus(tp.status)
        val wzStatus = mapStatus(wz.status)

        // 1. Status Disruption Priority: A fresh DOWN / CLOSED report takes precedence
        if (tpStatus == AttractionStatus.DOWN && tpTime >= wzTime - 600_000L) {
            return ResolvedAttraction(
                id = tp.id,
                parkId = parkId,
                name = tp.name,
                waitTimeMinutes = null,
                status = AttractionStatus.DOWN,
                primarySource = DataSource.THEMEPARKS_WIKI,
                isCrossVerified = false,
                comparisonNote = "ThemeParks.wiki meldet aktuelle Störung",
                lastUpdatedMillis = tpTime
            )
        }

        // 2. Consensus check: Both report operating and wait times are within 5 minutes
        val isConsensus = (tpWait != null && wzWait != null && abs(tpWait - wzWait) <= 5)

        // 3. Freshness check: Determine the newer source
        val (primaryWait, primarySource, note) = when {
            isConsensus -> {
                Triple(tpWait, DataSource.CROSS_VERIFIED, "Verifiziert über beide APIs")
            }
            tpTime > wzTime + 300_000L -> {
                Triple(
                    tpWait ?: wzWait,
                    DataSource.THEMEPARKS_WIKI,
                    "ThemeParks.wiki ist aktueller (${tpWait ?: "-"} min vs. Wartezeiten.APP ${wzWait ?: "-"} min)"
                )
            }
            wzTime > tpTime + 300_000L -> {
                Triple(
                    wzWait ?: tpWait,
                    DataSource.WARTEZEITEN_APP,
                    "Wartezeiten.APP ist aktueller (${wzWait ?: "-"} min vs. ThemeParks.wiki ${tpWait ?: "-"} min)"
                )
            }
            else -> {
                // If similar age, take the conservative (higher) wait time
                val chosenWait = maxOf(tpWait ?: 0, wzWait ?: 0)
                Triple(chosenWait, DataSource.CROSS_VERIFIED, "Beide APIs ähnlich aktuell")
            }
        }

        return ResolvedAttraction(
            id = tp.id,
            parkId = parkId,
            name = tp.name,
            waitTimeMinutes = primaryWait,
            status = if (primaryWait != null && primaryWait > 0) AttractionStatus.OPERATING else tpStatus,
            primarySource = primarySource,
            isCrossVerified = isConsensus,
            comparisonNote = note,
            lastUpdatedMillis = maxOf(tpTime, wzTime)
        )
    }

    private fun resolveThemeParksOnly(parkId: String, tp: LiveItemDto): ResolvedAttraction {
        val wait = tp.queue?.STANDBY?.waitTime
        return ResolvedAttraction(
            id = tp.id,
            parkId = parkId,
            name = tp.name,
            waitTimeMinutes = wait,
            status = mapStatus(tp.status),
            primarySource = DataSource.THEMEPARKS_WIKI,
            isCrossVerified = false,
            comparisonNote = "Exklusiv via ThemeParks.wiki",
            lastUpdatedMillis = parseIsoToMillis(tp.lastUpdated)
        )
    }

    private fun resolveWartezeitenOnly(parkId: String, wz: WartezeitenAttractionDto): ResolvedAttraction {
        return ResolvedAttraction(
            id = wz.resolveId(),
            parkId = parkId,
            name = wz.name,
            waitTimeMinutes = wz.waitTime,
            status = mapStatus(wz.status),
            primarySource = DataSource.WARTEZEITEN_APP,
            isCrossVerified = false,
            comparisonNote = "Exklusiv via Wartezeiten.APP",
            lastUpdatedMillis = parseIsoToMillis(wz.lastUpdated)
        )
    }

    private fun areNamesMatching(nameA: String, nameB: String): Boolean {
        if (nameA == nameB) return true
        if (nameA.contains(nameB) || nameB.contains(nameA)) return true
        return false
    }

    private fun normalize(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .replace("poweredby.*", "")
    }

    private fun mapStatus(status: String?): AttractionStatus {
        return when (status?.uppercase()) {
            "OPERATING", "OPEN" -> AttractionStatus.OPERATING
            "CLOSED" -> AttractionStatus.CLOSED
            "DOWN" -> AttractionStatus.DOWN
            "REFURBISHMENT", "MAINTENANCE" -> AttractionStatus.MAINTENANCE
            else -> AttractionStatus.UNKNOWN
        }
    }

    private fun parseIsoToMillis(isoString: String?): Long {
        if (isoString == null) return System.currentTimeMillis()
        return try {
            Instant.parse(isoString).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
