package de.exit.ridetrackr.data.engine

import de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity
import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.model.ResolvedAttraction
import java.time.DayOfWeek
import java.time.Instant
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
        RideSpec("tripsdrill-tausendfuessler", "Rasender Tausendfüßler", RideCategory.FAMILY_COASTER),
        RideSpec("tripsdrill-heiratsmarkt", "Heiratsmarkt", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-maibaum", "Maibaum", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-gugelhupf", "Gugelhupf", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-murmelturm", "Murmelturm", RideCategory.FLAT_RIDE_RELAXED),
        RideSpec("tripsdrill-altweibermuehle", "Altweibermühle", RideCategory.FLAT_RIDE_RELAXED)
    )

    fun generateAttractions(
        parkId: String,
        onlineCrowdLevel: Double? = null,
        dateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin")),
        trainingReports: List<UserWaitTimeReportEntity> = emptyList()
    ): List<ResolvedAttraction> {
        val month = dateTime.monthValue
        val dayOfMonth = dateTime.dayOfMonth
        val hour = dateTime.hour
        val dayOfWeek = dateTime.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

        // 1. Saison-Erkennung: Halloween & Lichtergaudi (Mitte Oktober bis Anfang November)
        // In dieser Zeit finden die "Schaurigen Altweibernächte" & "Lichtergaudi" statt mit Abendöffnung bis 20/21 Uhr
        val isHalloweenLichtergaudi = (month == 10 && dayOfMonth >= 15) || (month == 11 && dayOfMonth <= 5)

        // Öffnungszeiten: Standard 9-18 Uhr (letzter Einlass 17:xx), bei Lichtergaudi & Halloween bis 20/21 Uhr
        val maxOpenHour = if (isHalloweenLichtergaudi) {
            if (isWeekend || dayOfWeek == DayOfWeek.FRIDAY) 21 else 20
        } else {
            17
        }
        val isOpenHours = hour in 9..maxOpenHour
        val status = if (isOpenHours) AttractionStatus.OPERATING else AttractionStatus.CLOSED

        // 2. Saison- & Monatsfaktor (Tripsdrill Saison: Hochsommer = Peak, Winter = Pause/Nebensaison)
        val baseMonthFactor = when (month) {
            1, 2 -> 0.30       // Winterruhe
            3 -> 0.50          // Saisonstart Ende März
            4 -> 0.80          // Frühjahr / Osterferien
            5 -> 0.90          // Pfingstferien & Maimonate
            6 -> 1.10          // Frühsommer
            7 -> 1.40          // Sommerferien Beginn
            8 -> 1.55          // Hochsommer Peak (heißeste Tage, Hauptferien)
            9 -> 0.95          // Spätsommer
            10 -> 1.25         // Herbstferien, Halloween & Lichtergaudi
            11 -> if (dayOfMonth <= 5) 1.15 else 0.40 // Lichtergaudi-Finale
            12 -> 0.30         // Winterpause
            else -> 1.00
        }

        // Lichtergaudi- & Halloween-Event-Multiplikator: Deutlich mehr Besucher durch Sonderveranstaltungen
        val eventMultiplier = if (isHalloweenLichtergaudi) 1.30 else 1.0
        val monthFactor = baseMonthFactor * eventMultiplier

        // 3. Tageszeit-Faktor (Morgens wenig, mittags Peak, bei Lichtergaudi auch abends Dämmerungs-Peak)
        val hourFactor = when {
            !isOpenHours -> 0.0
            hour == 9 -> 0.35              // 09:00 - 09:59 (Parköffnung, noch sehr leer)
            hour in 10..11 -> 0.80         // 10:00 - 11:59 (Vormittag, Zulauf)
            hour in 12..13 -> 1.35         // 12:00 - 13:59 (Mittags-Peak!)
            hour in 14..15 -> 1.05         // 14:00 - 15:59 (Nachmittag, Entlastung)
            hour == 16 -> 1.20             // 16:00 - 16:59 (Zulauf zur Dämmerung)
            isHalloweenLichtergaudi && hour in 17..19 -> 1.35 // Lichterzauber, beleuchtete Achterbahnen & Nachtfahrten!
            isHalloweenLichtergaudi && hour in 20..21 -> 0.80 // Spätabend-Ausklang
            hour == 17 -> 0.40             // Vor normalem Parkschluss
            else -> 0.0
        }

        // 4. Wochentag-Faktor
        val dayFactor = if (isWeekend) 1.25 else 0.85

        // 5. Online Live-Crowd-Daten aus BaWü (z. B. 65% Auslastung)
        val onlineFactor = if (onlineCrowdLevel != null && onlineCrowdLevel > 0) {
            (onlineCrowdLevel / 50.0).coerceIn(0.5, 1.8)
        } else {
            1.0
        }

        val noteText = when {
            isHalloweenLichtergaudi && onlineCrowdLevel != null && onlineCrowdLevel > 0 ->
                "Live-Schätzung • Lichtergaudi & Halloween • ${onlineCrowdLevel.toInt()}% Auslastung"
            isHalloweenLichtergaudi ->
                "Live-Schätzung • Lichtergaudi & Halloween"
            onlineCrowdLevel != null && onlineCrowdLevel > 0 ->
                "Live-Schätzung • ${onlineCrowdLevel.toInt()}% Auslastung"
            else ->
                "Live-Schätzung"
        }

        val nowMillis = System.currentTimeMillis()

        return rides.map { ride ->
            val waitMinutes = if (!isOpenHours) {
                null
            } else {
                // 6. Wasserbahn-Saisoneffekt: Im Sommer besonders heiß begehrt, an kühlen Tagen kaum
                val waterModifier = if (ride.category.isWaterRide) {
                    when (month) {
                        in 6..8 -> 1.45
                        5, 9 -> 1.00
                        else -> 0.35 // Im kühlen Herbst/Halloween meiden viele Wasserbahnen
                    }
                } else {
                    1.00
                }

                val theoreticalMinutes = ride.category.baseWaitMinutes * monthFactor * hourFactor * dayFactor * onlineFactor * waterModifier

                // 7. Datenbank-Kalibrierung: Trainingsdaten aus historischen und Live-Meldungen einbeziehen
                val calibratedMinutes = calibrateWithTrainingReports(
                    ride = ride,
                    theoreticalMinutes = theoreticalMinutes,
                    month = month,
                    hour = hour,
                    trainingReports = trainingReports
                )

                // Runden auf 5-Minuten-Schritte und realistische Obergrenzen
                val rounded = (Math.round(calibratedMinutes / 5.0) * 5).toInt()
                val capped = when (ride.category) {
                    RideCategory.THRILL_COASTER -> rounded.coerceIn(5, 80)
                    RideCategory.WATER_RIDE -> rounded.coerceIn(5, 60)
                    RideCategory.FAMILY_COASTER -> rounded.coerceIn(5, 50)
                    RideCategory.FLAT_RIDE_MEDIUM -> rounded.coerceIn(5, 30)
                    RideCategory.FLAT_RIDE_RELAXED -> rounded.coerceIn(0, 20)
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
                isCrossVerified = onlineCrowdLevel != null || trainingReports.isNotEmpty(),
                comparisonNote = noteText,
                lastUpdatedMillis = nowMillis
            )
        }
    }

    /**
     * Trainiert / kalibriert die theoretische Schätzung anhand empirischer Meldungen aus der Datenbank.
     * Verwendet Bayesian Shrinkage mit gewichteter Ähnlichkeit zu Monat und Tageszeit.
     */
    private fun calibrateWithTrainingReports(
        ride: RideSpec,
        theoreticalMinutes: Double,
        month: Int,
        hour: Int,
        trainingReports: List<UserWaitTimeReportEntity>
    ): Double {
        if (trainingReports.isEmpty()) return theoreticalMinutes

        val matchingReports = trainingReports.filter {
            it.attractionId == ride.id || it.attractionName.equals(ride.name, ignoreCase = true)
        }
        if (matchingReports.isEmpty()) return theoreticalMinutes

        // Gewichtung der Trainingsmuster nach Saison- und Tageszeit-Nähe
        var weightedSum = 0.0
        var totalWeight = 0.0

        for (report in matchingReports) {
            val reportZdt = Instant.ofEpochMilli(report.reportedAtMillis).atZone(ZoneId.of("Europe/Berlin"))
            val rMonth = reportZdt.monthValue
            val rHour = reportZdt.hour

            // Saison-Ähnlichkeit (Monat)
            val monthDiff = Math.abs(rMonth - month).let { if (it > 6) 12 - it else it }
            val seasonWeight = when (monthDiff) {
                0 -> 2.5
                1 -> 1.5
                else -> 0.6
            }

            // Tageszeit-Ähnlichkeit (Stunde)
            val hourDiff = Math.abs(rHour - hour)
            val hourWeight = when {
                hourDiff <= 1 -> 2.0
                hourDiff <= 3 -> 1.2
                else -> 0.7
            }

            val sampleWeight = seasonWeight * hourWeight
            weightedSum += report.waitTimeMinutes * sampleWeight
            totalWeight += sampleWeight
        }

        if (totalWeight <= 0.0) return theoreticalMinutes

        val empiricalMean = weightedSum / totalWeight

        // Bayesian Shrinkage (Glättung):
        // Prior-Gewicht k = 2.0; je mehr Trainingsdaten vorliegen, desto stärker kalibriert die Datenbank
        val k = 2.0
        val effectiveN = (totalWeight / 2.5).coerceAtLeast(1.0)
        return ((k * theoreticalMinutes) + (effectiveN * empiricalMean)) / (k + effectiveN)
    }
}
