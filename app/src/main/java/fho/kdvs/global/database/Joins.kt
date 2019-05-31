package fho.kdvs.global.database

import androidx.room.Embedded
import androidx.room.Relation

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
