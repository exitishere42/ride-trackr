package de.exit.ridetrackr.data.engine

import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.model.ResolvedAttraction
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsdrillWaitTimeEngine @Inject constructor() {

    enum class RideCategory(val baseWaitMinutes: Int, val isWaterRide: Boolean = false) {
        THRILL_COASTER(25),      // Karacho, Mammut, Hals-über-Kopf
        FAMILY_COASTER(16),      // Volldampf, G'sengte Sau
        WATER_RIDE(20, true),    // Badewannen-Fahrt zum Jungbrunnen, Waschzuber-Rafting
        FLAT_RIDE_MEDIUM(10),    // Höhenflug, Doppelter Donnerbalken, Tausendfüßler
        FLAT_RIDE_RELAXED(6)     // Heiratsmarkt, Maibaum, Gugelhupf, Murmelturm, Altweibermühle
    }

    private data class RideSpec(
        val id: String,
        val name: String,
        val category: RideCategory
    )

    private val rides = listOf(
        RideSpec("tripsdrill-karacho", "Karacho", RideCategory.THRILL_COASTER),
        RideSpec("tripsdrill-mammut", "Mammut", RideCategory.THRILL_COASTER),
        RideSpec("tripsdrill-hals-ueber-kopf", "Hals-über-Kopf", RideCategory.THRILL_COASTER),
        RideSpec("tripsdrill-volldampf", "Volldampf", RideCategory.FAMILY_COASTER),
        RideSpec("tripsdrill-gsengte-sau", "G'sengte Sau", RideCategory.FAMILY_COASTER),
        RideSpec("tripsdrill-badewannen-fahrt", "Badewannen-Fahrt zum Jungbrunnen", RideCategory.WATER_RIDE),
        RideSpec("tripsdrill-waschzuber-rafting", "Waschzuber-Rafting", RideCategory.WATER_RIDE),
        RideSpec("tripsdrill-hoehenflug", "Höhenflug", RideCategory.FLAT_RIDE_MEDIUM),
        RideSpec("tripsdrill-donnerbalken", "Doppelter Donnerbalken", RideCategory.FLAT_RIDE_MEDIUM),
        RideSpec("tripsdrill-tausendfuessler", "Rasender Tausendfüßler", RideCategory.FLAT_RIDE_MEDIUM),
        RideSpec("tripsdrill-heiratsmarkt", "Heiratsmarkt", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-maibaum", "Maibaum", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-gugelhupf", "Gugelhupf", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-murmelturm", "Murmelturm", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-altweibermuehle", "Altweibermühle", RideCategory.FLAT_RIDE_RELAXED)
    )

    fun generateAttractions(
        parkId: String,
        onlineCrowdLevel: Double? = null,
        dateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
    ): List<ResolvedAttraction> {
        val month = dateTime.monthValue
        val hour = dateTime.hour
        val dayOfWeek = dateTime.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

        val isOpenHours = hour in 9..17 // Park geöffnet von 09:00 bis 18:00 (letzter Einlass 17:xx)
        val status = if (isOpenHours) AttractionStatus.OPERATING else AttractionStatus.CLOSED

        // 1. Saison- & Monatsfaktor (Tripsdrill Saison: Hochsommer = Peak, Winter = Pause/Nebensaison)
        val monthFactor = when (month) {
            1, 2 -> 0.30       // Winterruhe
            3 -> 0.50          // Saisonstart Ende März
            4 -> 0.80          // Frühjahr / Osterferien
            5 -> 0.90          // Pfingstferien & Maimonate
            6 -> 1.10          // Frühsommer
            7 -> 1.40          // Sommerferien Beginn
            8 -> 1.55          // Hochsommer Peak (heißeste Tage, Hauptferien)
            9 -> 0.95          // Spätsommer
            10 -> 1.20         // Herbstferien & Schaurige Altweibernächte (Halloween)
            11 -> 0.40         // Saisonabschluss Anfang November
            12 -> 0.30         // Winterpause
            else -> 1.00
        }

        // 2. Tageszeit-Faktor (Morgens wenig, mittags Peak, abends abflachend)
        val hourFactor = when {
            !isOpenHours -> 0.0
            hour == 9 -> 0.35              // 09:00 - 09:59 (Parköffnung, noch sehr leer)
            hour in 10..11 -> 0.80         // 10:00 - 11:59 (Vormittag, Zulauf)
            hour in 12..13 -> 1.35         // 12:00 - 13:59 (Mittags-Peak!)
            hour in 14..15 -> 1.05         // 14:00 - 15:59 (Nachmittag, Entlastung durch Gastronomie)
            hour == 16 -> 1.15             // 16:00 - 16:59 (Zweiter Nachmittags-Peak)
            hour == 17 -> 0.40             // 17:00 - 17:59 (Vor Parkschluss, Schlangen leeren sich)
            else -> 0.0
        }

        // 3. Wochentag-Faktor
        val dayFactor = if (isWeekend) 1.25 else 0.85

        // 4. Online Live-Crowd-Daten aus BaWü (z. B. 65% Auslastung)
        val onlineFactor = if (onlineCrowdLevel != null && onlineCrowdLevel > 0) {
            (onlineCrowdLevel / 50.0).coerceIn(0.5, 1.8)
        } else {
            1.0
        }

        val noteText = if (onlineCrowdLevel != null && onlineCrowdLevel > 0) {
            "Live-Schätzung • ${onlineCrowdLevel.toInt()}% Auslastung"
        } else {
            "Live-Schätzung"
        }

        val nowMillis = System.currentTimeMillis()

        return rides.map { ride ->
            val waitMinutes = if (!isOpenHours) {
                null
            } else {
                // 5. Wasserbahn-Saisoneffekt: Im Sommer besonders heiß begehrt, an kühlen Tagen kaum
                val waterModifier = if (ride.category.isWaterRide) {
                    when (month) {
                        in 6..8 -> 1.45
                        5, 9 -> 1.00
                        else -> 0.40
                    }
                } else {
                    1.00
                }

                val rawMinutes = ride.category.baseWaitMinutes * monthFactor * hourFactor * dayFactor * onlineFactor * waterModifier
                
                // Runden auf 5-Minuten-Schritte und realistische Obergrenzen
                val rounded = (Math.round(rawMinutes / 5.0) * 5).toInt()
                val capped = when (ride.category) {
                    RideCategory.THRILL_COASTER -> rounded.coerceIn(5, 75)
                    RideCategory.WATER_RIDE -> rounded.coerceIn(5, 60)
                    RideCategory.FAMILY_COASTER -> rounded.coerceIn(5, 45)
                    RideCategory.FLAT_RIDE_MEDIUM -> rounded.coerceIn(5, 25)
                    RideCategory.FLAT_RIDE_RELAXED -> rounded.coerceIn(0, 15)
                }
                capped
            }

            ResolvedAttraction(
                id = ride.id,
                parkId = parkId,
                name = ride.name,
                waitTimeMinutes = waitMinutes,
                status = status,
                primarySource = DataSource.THEMEPARKS_WIKI,
                isCrossVerified = onlineCrowdLevel != null,
                comparisonNote = noteText,
                lastUpdatedMillis = nowMillis
            )
        }
    }
}
