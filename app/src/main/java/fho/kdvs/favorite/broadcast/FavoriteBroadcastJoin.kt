package fho.kdvs.favorite.broadcast

import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteBroadcastEntity
import fho.kdvs.global.database.ShowEntity

/** Helper class to encapsulate a single favorited broadcast and its associated joins.*/
class FavoriteBroadcastJoin(
    val favorite: FavoriteBroadcastEntity?,
    val broadcast: BroadcastEntity?,
    val show: ShowEntity?
) {
    override fun equals(other: Any?): Boolean =
        this.favorite?.favoriteBroadcastId == (other as? FavoriteBroadcastJoin)?.favorite?.favoriteBroadcastId
}