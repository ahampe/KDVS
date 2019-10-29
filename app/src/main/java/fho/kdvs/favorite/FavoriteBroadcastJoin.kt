package fho.kdvs.favorite

import fho.kdvs.global.database.*

/** Helper class to encapsulate a single favorited broadcast and its associated joins.*/
class FavoriteBroadcastJoin (
    val favorite: FavoriteBroadcastEntity?,
    val broadcast: BroadcastEntity?,
    val show: ShowEntity?
)