package de.exit.ridetrackr.data.repository

import de.exit.ridetrackr.data.remote.api.WikipediaApi
import de.exit.ridetrackr.data.remote.api.WikipediaContentQuery
import de.exit.ridetrackr.data.remote.api.WikipediaContentResponse
import de.exit.ridetrackr.data.remote.api.WikipediaPageDto
import de.exit.ridetrackr.data.remote.api.WikipediaRevisionDto
import de.exit.ridetrackr.data.remote.api.WikipediaSearchItemDto
import de.exit.ridetrackr.data.remote.api.WikipediaSearchResultQuery
import de.exit.ridetrackr.data.remote.api.WikipediaSearchResponse
import de.exit.ridetrackr.data.remote.api.WikipediaSlotContainerDto
import de.exit.ridetrackr.data.remote.api.WikipediaSlotMainDto
import de.exit.ridetrackr.data.remote.api.WikipediaSummaryDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WikipediaRepositoryTest {

    private val fakeApi = object : WikipediaApi {
        override suspend fun search(query: String): WikipediaSearchResponse {
            return WikipediaSearchResponse(
                query = WikipediaSearchResultQuery(
                    search = listOf(
                        WikipediaSearchItemDto(title = "Karacho (Achterbahn)"),
                        WikipediaSearchItemDto(title = "Erlebnispark Tripsdrill")
                    )
                )
            )
        }

        override suspend fun getSummary(title: String): WikipediaSummaryDto {
            return WikipediaSummaryDto(
                title = "Karacho (Achterbahn)",
                extract = "Karacho ist eine Katapult-Achterbahn im Erlebnispark Tripsdrill.",
                description = "Achterbahn in Baden-Württemberg"
            )
        }

        override suspend fun getArticleContent(title: String): WikipediaContentResponse {
            val wikitext = """
                | MODELL = [[Infinity Coaster]]
                | HERSTELLER = [[Gerstlauer Amusement Rides]]
                | GESCHWINDIGKEIT = 100
                | HÖHE = 30
                | LÄNGE = 700
                | INVERSIONEN = 4
                | ERÖFFNUNG = 2013
            """.trimIndent()

            return WikipediaContentResponse(
                query = WikipediaContentQuery(
                    pages = mapOf(
                        "123" to WikipediaPageDto(
                            title = title,
                            revisions = listOf(
                                WikipediaRevisionDto(
                                    slots = WikipediaSlotContainerDto(
                                        main = WikipediaSlotMainDto(content = wikitext)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun testGetRideData_parsesWikipediaSuccessfully() = runBlocking {
        val repo = WikipediaRepository(fakeApi)
        val result = repo.getRideData("Karacho", "Erlebnispark Tripsdrill")

        assertNotNull(result)
        assertEquals("Karacho (Achterbahn)", result?.title)
        assertEquals("Gerstlauer Amusement Rides", result?.manufacturer)
        assertEquals(100, result?.topSpeedKmh)
        assertEquals(30, result?.heightMeters)
        assertEquals(700, result?.trackLengthMeters)
        assertEquals(4, result?.inversions)
        assertEquals("Infinity Coaster", result?.model)
        assertEquals("2013", result?.openingDate)
        assertTrue(result?.description?.contains("Karacho") == true)
    }

    @Test
    fun testGetRideData_returnsNullForMismatchedSearchHits() = runBlocking {
        val mismatchedApi = object : WikipediaApi {
            override suspend fun search(query: String): WikipediaSearchResponse {
                // Return unrelated articles like generic coaster model or park article
                return WikipediaSearchResponse(
                    query = WikipediaSearchResultQuery(
                        search = listOf(
                            WikipediaSearchItemDto(title = "Tivoli (Achterbahnmodell)"),
                            WikipediaSearchItemDto(title = "Erlebnispark Tripsdrill")
                        )
                    )
                )
            }
            override suspend fun getSummary(title: String): WikipediaSummaryDto = WikipediaSummaryDto(title = title)
            override suspend fun getArticleContent(title: String): WikipediaContentResponse = WikipediaContentResponse()
        }

        val repo = WikipediaRepository(mismatchedApi)
        val result = repo.getRideData("Rasender Tausendfüßler", "Erlebnispark Tripsdrill")

        // Must be null to avoid displaying wrong Wikipedia text!
        org.junit.Assert.assertNull(result)
    }
}
