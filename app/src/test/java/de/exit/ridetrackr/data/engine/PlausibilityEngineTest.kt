package de.exit.ridetrackr.data.engine

import de.exit.ridetrackr.data.model.AttractionStatus
import de.exit.ridetrackr.data.model.DataSource
import de.exit.ridetrackr.data.remote.api.LiveItemDto
import de.exit.ridetrackr.data.remote.api.QueueDto
import de.exit.ridetrackr.data.remote.api.StandbyQueueDto
import de.exit.ridetrackr.data.remote.api.WartezeitenAttractionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlausibilityEngineTest {

    private val engine = PlausibilityEngine()

    @Test
    fun `consensus when both apis report similar wait time`() {
        val tp = listOf(
            LiveItemDto(
                id = "tp-1",
                name = "Voltron Nevera",
                entityType = "ATTRACTION",
                status = "OPERATING",
                queue = QueueDto(STANDBY = StandbyQueueDto(waitTime = 25)),
                lastUpdated = "2026-10-27T17:00:00Z"
            )
        )
        val wz = listOf(
            WartezeitenAttractionDto(
                id = "wz-1",
                name = "Voltron Nevera",
                waitTime = 25,
                status = "OPERATING",
                lastUpdated = "2026-10-27T17:02:00Z"
            )
        )

        val resolved = engine.resolve("europa-park", tp, wz)
        assertEquals(1, resolved.size)
        val item = resolved.first()
        assertEquals(25, item.waitTimeMinutes)
        assertTrue(item.isCrossVerified)
        assertEquals(DataSource.CROSS_VERIFIED, item.primarySource)
    }

    @Test
    fun `fresher timestamp wins when wait times diverge`() {
        val tp = listOf(
            LiveItemDto(
                id = "tp-bluefire",
                name = "Blue Fire Megacoaster",
                entityType = "ATTRACTION",
                status = "OPERATING",
                queue = QueueDto(STANDBY = StandbyQueueDto(waitTime = 10)),
                lastUpdated = "2026-10-27T17:25:00Z" // 25 Min nach wz
            )
        )
        val wz = listOf(
            WartezeitenAttractionDto(
                id = "wz-bluefire",
                name = "blue fire Megacoaster",
                waitTime = 35,
                status = "OPERATING",
                lastUpdated = "2026-10-27T17:00:00Z" // Veraltet
            )
        )

        val resolved = engine.resolve("europa-park", tp, wz)
        val item = resolved.first()
        assertEquals(10, item.waitTimeMinutes)
        assertEquals(DataSource.THEMEPARKS_WIKI, item.primarySource)
    }

    @Test
    fun `tripsdrill attraction exclusively from themeparks wiki is resolved`() {
        val tp = listOf(
            LiveItemDto(
                id = "tp-karacho",
                name = "Karacho",
                entityType = "ATTRACTION",
                status = "OPERATING",
                queue = QueueDto(STANDBY = StandbyQueueDto(waitTime = 15)),
                lastUpdated = "2026-10-27T17:10:00Z"
            )
        )
        val wz = emptyList<WartezeitenAttractionDto>()

        val resolved = engine.resolve("tripsdrill", tp, wz)
        assertEquals(1, resolved.size)
        val item = resolved.first()
        assertEquals("Karacho", item.name)
        assertEquals(15, item.waitTimeMinutes)
        assertEquals(DataSource.THEMEPARKS_WIKI, item.primarySource)
        assertEquals(AttractionStatus.OPERATING, item.status)
    }
}
