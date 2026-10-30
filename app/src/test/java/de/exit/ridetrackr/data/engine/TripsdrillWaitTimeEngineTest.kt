package de.exit.ridetrackr.data.engine

import de.exit.ridetrackr.data.model.AttractionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TripsdrillWaitTimeEngineTest {

    private val engine = TripsdrillWaitTimeEngine()
    private val zone = ZoneId.of("Europe/Berlin")

    @Test
    fun testMittagVielMorgensWenig() {
        // Same day in August (Summer), compare 09:15 morning with 13:00 peak noon
        val morningTime = ZonedDateTime.of(2026, 8, 5, 9, 15, 0, 0, zone)
        val noonTime = ZonedDateTime.of(2026, 8, 5, 13, 0, 0, 0, zone)

        val morningList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 60.0, dateTime = morningTime)
        val noonList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 60.0, dateTime = noonTime)

        val karachoMorning = morningList.first { it.name == "Karacho" }
        val karachoNoon = noonList.first { it.name == "Karacho" }

        assertNotNull(karachoMorning.waitTimeMinutes)
        assertNotNull(karachoNoon.waitTimeMinutes)

        assertTrue(
            "Karacho mittags ($karachoNoon) muss signifikant mehr Wartezeit haben als morgens ($karachoMorning)",
            karachoNoon.waitTimeMinutes!! > karachoMorning.waitTimeMinutes!!
        )
    }

    @Test
    fun testSommerMehrAlsWinter() {
        // Compare same hour (13:00) in August (Summer) vs. January (Winter)
        val summerTime = ZonedDateTime.of(2026, 7, 15, 13, 0, 0, 0, zone)
        val winterTime = ZonedDateTime.of(2026, 1, 15, 13, 0, 0, 0, zone)

        val summerList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 50.0, dateTime = summerTime)
        val winterList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 50.0, dateTime = winterTime)

        val mammutSummer = summerList.first { it.name == "Mammut" }
        val mammutWinter = winterList.first { it.name == "Mammut" }

        assertTrue(
            "Mammut im Sommer (${mammutSummer.waitTimeMinutes} min) muss mehr Wartezeit haben als im Winter (${mammutWinter.waitTimeMinutes} min)",
            mammutSummer.waitTimeMinutes!! > mammutWinter.waitTimeMinutes!!
        )
    }

    @Test
    fun testOnlineCrowdLevelInfluence() {
        val time = ZonedDateTime.of(2026, 6, 10, 12, 30, 0, 0, zone)

        val highCrowdList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 85.0, dateTime = time)
        val lowCrowdList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 25.0, dateTime = time)

        val karachoHigh = highCrowdList.first { it.name == "Karacho" }
        val karachoLow = lowCrowdList.first { it.name == "Karacho" }

        assertTrue(
            "Bei hohem Online-Crowdlevel 85% (${karachoHigh.waitTimeMinutes} min) muss die Wartezeit höher sein als bei 25% (${karachoLow.waitTimeMinutes} min)",
            karachoHigh.waitTimeMinutes!! > karachoLow.waitTimeMinutes!!
        )
        assertTrue(karachoHigh.comparisonNote?.contains("85%") == true)
    }

    @Test
    fun testWaterRideSummerBoost() {
        val summerTime = ZonedDateTime.of(2026, 7, 20, 13, 0, 0, 0, zone)
        val autumnTime = ZonedDateTime.of(2026, 10, 20, 13, 0, 0, 0, zone)

        val summerList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 50.0, dateTime = summerTime)
        val autumnList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 50.0, dateTime = autumnTime)

        val flumeSummer = summerList.first { it.name.contains("Badewannen") }
        val flumeAutumn = autumnList.first { it.name.contains("Badewannen") }

        assertTrue(
            "Badewannenfahrt muss im heißen Sommer (${flumeSummer.waitTimeMinutes} min) deutlich mehr Wartezeit haben als im kühlen Herbst (${flumeAutumn.waitTimeMinutes} min)",
            flumeSummer.waitTimeMinutes!! > flumeAutumn.waitTimeMinutes!!
        )
    }

    @Test
    fun testClosedOutsideParkHours() {
        // 22:00 at night
        val nightTime = ZonedDateTime.of(2026, 8, 1, 22, 0, 0, 0, zone)
        val list = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 50.0, dateTime = nightTime)

        assertTrue(list.all { it.status == AttractionStatus.CLOSED })
        assertTrue(list.all { it.waitTimeMinutes == null })
    }

    @Test
    fun testHalloweenLichtergaudiEventBoost() {
        // October 30 at 17:30 during Lichtergaudi
        val lichtergaudiTime = ZonedDateTime.of(2026, 10, 30, 17, 30, 0, 0, zone)
        val regularTime = ZonedDateTime.of(2026, 9, 15, 17, 0, 0, 0, zone)

        val eventList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 60.0, dateTime = lichtergaudiTime)
        val regularList = engine.generateAttractions("tripsdrill", onlineCrowdLevel = 60.0, dateTime = regularTime)

        val karachoEvent = eventList.first { it.name == "Karacho" }
        val karachoRegular = regularList.first { it.name == "Karacho" }

        // Must be open during Lichtergaudi evening!
        assertEquals(AttractionStatus.OPERATING, karachoEvent.status)
        assertNotNull(karachoEvent.waitTimeMinutes)

        // Event wait time must be higher due to Lichtergaudi & Halloween popularity
        assertTrue(
            "Karacho während Lichtergaudi (${karachoEvent.waitTimeMinutes} min) muss höher sein als an regulären Tagen (${karachoRegular.waitTimeMinutes} min)",
            karachoEvent.waitTimeMinutes!! > karachoRegular.waitTimeMinutes!!
        )
        assertTrue(karachoEvent.comparisonNote?.contains("Lichtergaudi") == true)
    }

    @Test
    fun testDatabaseTrainingCalibration() {
        val testTime = ZonedDateTime.of(2026, 10, 30, 17, 0, 0, 0, zone)

        // Generate baseline without training data
        val baselineList = engine.generateAttractions("tripsdrill", dateTime = testTime, trainingReports = emptyList())
        val karachoBaseline = baselineList.first { it.name == "Karacho" }.waitTimeMinutes ?: 0

        // Provide database training reports where users reported higher wait times (e.g. 55 min)
        val training = listOf(
            de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity(
                id = 1L,
                parkId = "tripsdrill",
                attractionId = "tripsdrill-karacho",
                attractionName = "Karacho",
                waitTimeMinutes = 55,
                reportedAtMillis = testTime.toInstant().toEpochMilli() - 3600000L // 1 hour ago
            ),
            de.exit.ridetrackr.data.local.entity.UserWaitTimeReportEntity(
                id = 2L,
                parkId = "tripsdrill",
                attractionId = "tripsdrill-karacho",
                attractionName = "Karacho",
                waitTimeMinutes = 55,
                reportedAtMillis = testTime.toInstant().toEpochMilli() - 7200000L // 2 hours ago
            )
        )

        val calibratedList = engine.generateAttractions("tripsdrill", dateTime = testTime, trainingReports = training)
        val karachoCalibrated = calibratedList.first { it.name == "Karacho" }.waitTimeMinutes ?: 0

        // Calibrated wait time must move towards the database training samples
        assertTrue(
            "Kalibrierte Wartezeit ($karachoCalibrated min) muss höher sein als die reine Theorie ($karachoBaseline min)",
            karachoCalibrated > karachoBaseline
        )
    }
}
