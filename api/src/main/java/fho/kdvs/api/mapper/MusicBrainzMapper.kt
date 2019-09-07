package fho.kdvs.api.mapper

import fho.kdvs.api.mapped.MusicBrainzAlbum
import fho.kdvs.api.raw.album.MusicBrainzAlbumsResponse

/**
 * Class which maps MusicBrainz API responses into client-usable classes.
 */
class MusicBrainzMapper {

    fun album(response: MusicBrainzAlbumsResponse?): MusicBrainzAlbum? {
        return response?.releases?.firstOrNull()?.let {
            val label = it.labels?.firstOrNull()?.label?.name.nullIfBlank()
            val year = it.date?.toIntOrNull()
            MusicBrainzAlbum(name = it.title.nullIfBlank(), id = it.id.nullIfBlank(),
                year = year, label = label)
        }
    }

}
