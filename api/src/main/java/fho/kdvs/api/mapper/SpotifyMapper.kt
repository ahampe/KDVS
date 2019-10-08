package fho.kdvs.api.mapper

import fho.kdvs.api.mapped.*
import fho.kdvs.api.raw.SpotifyPlaylistResponse
import fho.kdvs.api.raw.album.SpotifyAlbumResponse
import fho.kdvs.api.raw.album.SpotifySimpleAlbumResponse
import fho.kdvs.api.raw.album.SpotifySimpleAlbumsResponse
import fho.kdvs.api.raw.objects.*
import fho.kdvs.api.raw.track.SpotifyTracksResponse

/**
 * Class which maps Spotify API responses into client-usable classes.
 */
class SpotifyMapper {

    fun album(response: SpotifySimpleAlbumsResponse?): SpotifySimpleAlbum? {
        return response?.albums?.items?.firstOrNull()?.let {
            val year = it.releaseDate.take(4).toIntOrNull()
            val imageHref = it.images.firstOrNull()?.url.nullIfBlank()
            SpotifySimpleAlbum(id = it.id, name = it.name, uri = it.uri, year = year,
                imageHref = imageHref.nullIfBlank())
        }
    }

    fun album(response: SpotifySimpleAlbumResponse?): SpotifySimpleAlbum? {
        return response?.let {
            val year = it.releaseDate.take(4).toIntOrNull()
            val imageHref = it.images.firstOrNull()?.url.nullIfBlank()
            SpotifySimpleAlbum(id = it.id, name = it.name, uri = it.uri, year = year,
                imageHref = imageHref.nullIfBlank())
        }
    }

    fun album(response: SpotifyAlbumResponse?): SpotifyAlbum? {
        return response?.let {
            val year = it.releaseDate.take(4).toIntOrNull()
            val imageHref = it.images.firstOrNull()?.url.nullIfBlank()
            SpotifyAlbum(id = it.id, name = it.name, uri = it.uri, year = year,
                imageHref = imageHref.nullIfBlank(), tracks = it.tracks?.items?.map { t -> track(t)})
        }
    }

    fun track(response: SpotifyTracksResponse?): SpotifyTrack? {
        return response?.let {
            it.tracks.items.firstOrNull()?.let { t ->
                SpotifyTrack(id = t.id, uri = t.uri, name = t.name)
            }
        }
    }

    fun track(track: SpotifyTrackObject?): SpotifyTrack? {
        return track?.let {
            SpotifyTrack(id = it.id, uri = it.uri, name = it.name)
        }
    }

    fun track(track: SpotifySimplifiedTrackObject?): SpotifyTrack? {
        return track?.let {
            SpotifyTrack(id = it.id, uri = it.uri, name = it.name)
        }
    }

    fun track(track: SpotifyPlaylistTrackObject?): SpotifyTrack? {
        return track?.let {
            SpotifyTrack(id = it.track.id, uri = it.track.uri, name = it.track.name)
        }
    }

    fun tracks(response: SpotifyPager<SpotifyPlaylistTrackObject>?): List<SpotifyTrack?>? {
        return response?.let {
            it.items.map { i -> track(i)}
        }
    }

    fun playlist(response: SpotifyPlaylistResponse?): SpotifyPlaylist? {
        return response?.let {
            SpotifyPlaylist(uri = it.uri, id = it.id, name = it.name, count = it.tracks?.total ?: 0)
        }
    }

    fun playlists(response: SpotifyPager<SpotifyPlaylistResponse>?): List<SpotifyPlaylist?>? {
        return response?.let {
            it.items.map { i -> playlist(i)}
        }
    }

    fun profile(response: SpotifyPrivateUserObject?): SpotifyProfile? {
        return response?.let {
            SpotifyProfile(id = it.id, name = it.display_name)
        }
    }

    fun error(response: SpotifyErrorObject?): SpotifyError? {
        return response?.let {
            SpotifyError(status = it.status, message = it.message)
        }
    }
}
