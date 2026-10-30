package de.exit.ridetrackr.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApi {

    @GET("w/api.php?action=query&list=search&utf8=&format=json")
    suspend fun search(
        @Query("srsearch") query: String
    ): WikipediaSearchResponse

    @GET("api/rest_v1/page/summary/{title}")
    suspend fun getSummary(
        @Path("title") title: String
    ): WikipediaSummaryDto

    @GET("w/api.php?action=query&prop=revisions&rvslots=*&rvprop=content&format=json")
    suspend fun getArticleContent(
        @Query("titles") title: String
    ): WikipediaContentResponse
}

@Serializable
data class WikipediaSearchResponse(
    val query: WikipediaSearchResultQuery? = null
)

@Serializable
data class WikipediaSearchResultQuery(
    val search: List<WikipediaSearchItemDto> = emptyList()
)

@Serializable
data class WikipediaSearchItemDto(
    val title: String,
    val snippet: String? = null,
    val pageid: Long? = null
)

@Serializable
data class WikipediaSummaryDto(
    val title: String? = null,
    val displaytitle: String? = null,
    val description: String? = null,
    val extract: String? = null,
    val thumbnail: WikipediaImageDto? = null,
    val originalimage: WikipediaImageDto? = null,
    @SerialName("content_urls")
    val contentUrls: WikipediaContentUrlsDto? = null
)

@Serializable
data class WikipediaImageDto(
    val source: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class WikipediaContentUrlsDto(
    val desktop: WikipediaDesktopUrlDto? = null
)

@Serializable
data class WikipediaDesktopUrlDto(
    val page: String? = null
)

@Serializable
data class WikipediaContentResponse(
    val query: WikipediaContentQuery? = null
)

@Serializable
data class WikipediaContentQuery(
    val pages: Map<String, WikipediaPageDto> = emptyMap()
)

@Serializable
data class WikipediaPageDto(
    val title: String? = null,
    val revisions: List<WikipediaRevisionDto> = emptyList()
)

@Serializable
data class WikipediaRevisionDto(
    val slots: WikipediaSlotContainerDto? = null
)

@Serializable
data class WikipediaSlotContainerDto(
    val main: WikipediaSlotMainDto? = null
)

@Serializable
data class WikipediaSlotMainDto(
    @SerialName("*")
    val content: String? = null
)
