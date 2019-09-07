package fho.kdvs.api.endpoint

import fho.kdvs.api.raw.artwork.MusicBrainzAlbumArtResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Companion endpoint for MusicBrainz. Used to retrieve album art
 */
interface CoverArtArchiveEndpoint {

    /**
     * Retrieves the album art for a given MusicBrainz release [id]
     */
    @GET("{id}")
    suspend fun getAlbumArtHref(@Path("id") id: String): Response<MusicBrainzAlbumArtResponse>

    companion object {
        const val BASE_URL = "http://coverartarchive.org/release/"
    }

}
