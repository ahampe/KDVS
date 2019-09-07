package fho.kdvs.api.mapper

import fho.kdvs.api.mapped.SpotifyAlbum
import fho.kdvs.api.raw.album.SpotifyAlbumsResponse

/**
 * Class which maps Spotify API responses into client-usable classes.
 */
class SpotifyMapper {

    fun album(response: SpotifyAlbumsResponse?): SpotifyAlbum? {
        return response?.albums?.items?.firstOrNull()?.let {
            val year = it.releaseDate.take(4).toIntOrNull()
            val imageHref = it.images.firstOrNull()?.url.nullIfBlank()
            SpotifyAlbum(name = it.name, uri = it.uri, year = year,
                imageHref = imageHref.nullIfBlank())
        }
    }

}
