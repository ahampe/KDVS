package fho.kdvs.global.database.joins

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.favorite.track.FavoriteTrackJoin
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.FavoriteTrackEntity
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity

/**
 * Classes to encapsulate the joins between [FavoriteTrackEntity], its associated [TrackEntity],
 * the [BroadcastEntity] on which the track aired, and the broadcast's corresponding [ShowEntity].
 * */
class ShowBroadcastTrackFavoriteJoin : Join() {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = BroadcastEntity::class)
    var broadcastTrackFavorite: List<BroadcastTrackFavoriteJoin> = ArrayList()
}

/**
 * Helper functions to return specific data types from [ShowBroadcastTrackFavoriteJoin].
 * */
fun ShowBroadcastTrackFavoriteJoin.getBroadcasts(): List<BroadcastEntity?> {
    return this.broadcastTrackFavorite
        .map { it.broadcast }
        .toList()
        .distinct()
}

fun ShowBroadcastTrackFavoriteJoin.getTracks(): List<TrackEntity?> {
    return this.broadcastTrackFavorite
        .flatMap {
            it.trackFavorite
                .map { tf -> tf.track }
        }.toList()
        .distinct()
}

fun ShowBroadcastTrackFavoriteJoin.getFavorites(): List<FavoriteTrackEntity?> {
    return this.broadcastTrackFavorite
        .flatMap {
            it.trackFavorite
                .flatMap { tf -> tf.favorite }
        }.toList()
        .distinct()
}

fun List<ShowBroadcastTrackFavoriteJoin>?.getBroadcasts(): List<BroadcastEntity?>? =
    this?.flatMap { f -> f.getBroadcasts() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getTracks(): List<TrackEntity?>? =
    this?.flatMap { f -> f.getTracks() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getFavorites(): List<FavoriteTrackEntity?>? =
    this?.flatMap { f -> f.getFavorites() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getTrackFavoriteJoins(): List<FavoriteTrackJoin>? {
    val results = mutableListOf<FavoriteTrackJoin>()

    val shows = this
        ?.map { it.show }
        ?.distinct()
    val broadcasts = this
        ?.flatMap { it.getBroadcasts() }
        ?.distinct()
    val tracks = this
        ?.flatMap { it.getTracks() }
        ?.distinct()
    val favorites = this
        ?.flatMap { it.getFavorites() }
        ?.distinct()

    favorites?.forEach { favorite ->
        val track = tracks
            ?.firstOrNull {
                it?.trackId == favorite?.trackId
            }
        val broadcast = broadcasts
            ?.firstOrNull {
                it?.broadcastId == track?.broadcastId
            }
        val show = shows
            ?.firstOrNull {
                it?.id == broadcast?.showId
            }

        results.add(FavoriteTrackJoin(favorite, track, broadcast, show))
    }

    return results
}