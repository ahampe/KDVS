package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.favorite.broadcast.FavoriteBroadcastJoin
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteBroadcastEntity
import fho.kdvs.global.database.ShowEntity

/**
 * Classes to encapsulate the joins between [FavoriteBroadcastEntity], its [BroadcastEntity],
 * and the broadcast's corresponding [ShowEntity].
 * */
class ShowBroadcastFavoriteJoin: Join() {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = BroadcastEntity::class)
    var broadcastFavorite: List<BroadcastFavoriteJoin> = ArrayList()
}

/**
 * Helper functions to return specific data types from [ShowBroadcastFavoriteJoin].
 * */
fun ShowBroadcastFavoriteJoin.getBroadcasts(): List<BroadcastEntity?> {
    return this.broadcastFavorite
        .map { it.broadcast }
        .toList()
        .distinct()
}

fun ShowBroadcastFavoriteJoin.getFavorites(): List<FavoriteBroadcastEntity?> {
    return this.broadcastFavorite
        .flatMap { it.favorite }
        .toList()
        .distinct()
}

fun List<ShowBroadcastFavoriteJoin>?.getBroadcastFavoriteJoins(): List<FavoriteBroadcastJoin>? {
    val results = mutableListOf<FavoriteBroadcastJoin>()

    val shows = this
        ?.map { it.show }
        ?.distinct()
    val broadcasts = this
        ?.flatMap { it.getBroadcasts() }
        ?.distinct()
    val favorites = this
        ?.flatMap { it.getFavorites() }
        ?.distinct()

    favorites?.forEach { favorite ->
        val broadcast = broadcasts
            ?.firstOrNull {
                it?.broadcastId == favorite?.broadcastId
            }
        val show = shows
            ?.firstOrNull {
                it?.id == broadcast?.showId
            }

        results.add(FavoriteBroadcastJoin(favorite, broadcast, show))
    }

    return results
}