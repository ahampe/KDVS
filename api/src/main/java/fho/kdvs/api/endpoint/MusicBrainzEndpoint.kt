package fho.kdvs.api.endpoint

import fho.kdvs.api.raw.album.MusicBrainzAlbumsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicBrainzEndpoint {

    /**
     * Searches MusicBrainz database for albums matching the query... refer to their documentation
     */
    @GET("/ws/2/release?inc=labels&fmt=json")
    suspend fun searchAlbums(@Query("query") query: String): Response<MusicBrainzAlbumsResponse>

    companion object {
        const val BASE_URL = "https://musicbrainz.org"
    }

}
