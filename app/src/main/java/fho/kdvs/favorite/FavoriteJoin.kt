package fho.kdvs.favorite

import fho.kdvs.global.database.*

/** Helper class to encapsulate a single favorited track and its associated joins.*/
class FavoriteJoin (
    val favorite: FavoriteEntity?,
    val track: TrackEntity?,
    val broadcast: BroadcastEntity?,
    val show: ShowEntity?
)