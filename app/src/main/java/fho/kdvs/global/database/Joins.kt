package fho.kdvs.global.database

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.favorite.FavoriteJoin

/**
 * Classes to encapsulate the joins between [FavoriteEntity], its associated [TrackEntity],
 * the [BroadcastEntity] on which the track aired, and the broadcast's corresponding [ShowEntity].
 * */
class ShowBroadcastTrackFavoriteJoin {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = BroadcastEntity::class)
    var broadcastTrackFavorite: List<BroadcastTrackFavoriteJoin> = ArrayList()
}

class BroadcastTrackFavoriteJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId", entity = TrackEntity::class)
    var trackFavorite: List<TrackFavoriteJoin> = ArrayList()
}

class TrackFavoriteJoin {
    @Embedded
    var track: TrackEntity? = null

    @Relation(parentColumn = "trackId", entityColumn = "trackId")
    var favorite: List<FavoriteEntity> = ArrayList()
}

class ShowBroadcastJoin {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId")
    var broadcast: List<BroadcastEntity> = ArrayList()
}

class BroadcastTrackJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId")
    var track: List<TrackEntity> = ArrayList()
}

fun ShowBroadcastTrackFavoriteJoin.getBroadcasts(): List<BroadcastEntity?> {
   return this.broadcastTrackFavorite
       .map{ it.broadcast }
       .toList()
       .distinct()
}

fun ShowBroadcastTrackFavoriteJoin.getTracks(): List<TrackEntity?> {
    return this.broadcastTrackFavorite
        .flatMap { it.trackFavorite
            .map { tf -> tf.track }
        }.toList()
        .distinct()
}

fun ShowBroadcastTrackFavoriteJoin.getFavorites(): List<FavoriteEntity?> {
    return this.broadcastTrackFavorite
        .flatMap { it.trackFavorite
            .flatMap { tf -> tf.favorite }
        }.toList()
        .distinct()
}

fun List<ShowBroadcastTrackFavoriteJoin>?.getBroadcasts(): List<BroadcastEntity?>? =
    this?.flatMap { f -> f.getBroadcasts() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getTracks(): List<TrackEntity?>? =
    this?.flatMap { f -> f.getTracks() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getFavorites(): List<FavoriteEntity?>? =
    this?.flatMap { f -> f.getFavorites() }

fun List<ShowBroadcastTrackFavoriteJoin>?.getFavoriteJoins(): List<FavoriteJoin>? {
    val results = mutableListOf<FavoriteJoin>()

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
        ?.flatMap { it.getFavorites()}
        ?.distinct()

    favorites?.forEach { favorite ->
        val track = tracks
            ?.firstOrNull{
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

        results.add(FavoriteJoin(favorite, track, broadcast, show))
    }

    return results
}