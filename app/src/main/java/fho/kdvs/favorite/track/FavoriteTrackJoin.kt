package fho.kdvs.favorite.track

import fho.kdvs.global.database.*

/** Helper class to encapsulate a single favorited track and its associated joins.*/
class FavoriteTrackJoin (
    val favorite: FavoriteTrackEntity?,
    val track: TrackEntity?,
    val broadcast: BroadcastEntity?,
    val show: ShowEntity?
) {
    override fun equals(other: Any?): Boolean =
        this.favorite?.favoriteTrackId == (other as? FavoriteTrackJoin)?.favorite?.favoriteTrackId
}