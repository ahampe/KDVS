package fho.kdvs.api.service

import dagger.Reusable
import fho.kdvs.api.endpoint.CoverArtArchiveEndpoint
import fho.kdvs.api.endpoint.MusicBrainzEndpoint
import fho.kdvs.api.mapped.MusicBrainzAlbum
import fho.kdvs.api.mapper.MusicBrainzMapper
import fho.kdvs.api.mapper.nullIfBlank
import fho.kdvs.base.urlEncoded
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

/**
 * Client-side class for requesting info from MusicBrainz. There is no need for any saved state
 * when working with this API, so this is not required to be a singleton.
 */
@Reusable
class MusicBrainzService @Inject constructor(
    private val mbEndpoint: MusicBrainzEndpoint,
    private val caEndpoint: CoverArtArchiveEndpoint
) : BaseService() {

    private val mapper = MusicBrainzMapper()

    suspend fun findAlbumsAsync(album: String?,
                                artist: String?): Deferred<MusicBrainzAlbum?> = coroutineScope {
        async {
            if (album.isNullOrBlank() || artist.isNullOrBlank()) return@async null

            val query = makeAlbumQuery(album, artist)
            val response = mbEndpoint.searchAlbums(query)
            if (response.isSuccessful) {
                return@async mapper.album(response.body())
            } else {
                Timber.d("Error searching MusicBrainz for $album and $artist")
                Timber.d(response.message())
                response.errorBody()?.let { Timber.d(it.string()) }
                return@async null
            }
        }
    }

    suspend fun getAlbumArtHrefAsync(releaseId: String): Deferred<String?> = coroutineScope {
        async {
            val response = caEndpoint.getAlbumArtHref(releaseId)
            if (response.isSuccessful) {
                return@async response.body()?.images?.firstOrNull()?.imageHref.nullIfBlank()
            } else {
                Timber.d("Error searching cover art archive")
                Timber.d(response.message())
                response.errorBody()?.let { Timber.d(it.string()) }
                return@async null
            }
        }
    }

    private fun makeAlbumQuery(album: String, artist: String): String {
        val albumQuery = fuzzyEncode(album)
        val artistQuery = fuzzyEncode(artist)
        return "\"$albumQuery\" AND artist:\"$artistQuery\""
    }

    /**
     * Appends '~' to each word to enable fuzzy search.
     */
    private fun fuzzyEncode(name: String): String {
        return name.urlEncoded
            .replace(" ", " ~")
            .replace("+", "~+") + "~"
    }

}
