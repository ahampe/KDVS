package fho.kdvs.global.database

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Classes to encapsulate the joins between [FavoriteEntity], its associated [TrackEntity],
 * the [BroadcastEntity] on which the track aired, and the broadcast's corresponding [ShowEntity].
 * */
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

class TrackFavoriteJoin {
    @Embedded
    var track: TrackEntity? = null

    @Relation(parentColumn = "trackId", entityColumn = "trackId")
    var favorite: List<FavoriteEntity> = ArrayList()
}

class BroadcastTrackFavoriteJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId", entity = TrackEntity::class)
    var trackFavorite: List<TrackFavoriteJoin> = ArrayList()
}

class ShowBroadcastTrackFavoriteJoin {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = BroadcastEntity::class)
    var broadcastTrackFavorite: List<BroadcastTrackFavoriteJoin> = ArrayList()
}


fun ShowBroadcastTrackFavoriteJoin.getBroadcast(): BroadcastEntity? {
   return this.broadcastTrackFavorite
       .firstOrNull()
       ?.broadcast
}

fun ShowBroadcastTrackFavoriteJoin.getTrack(): TrackEntity? {
    return this.broadcastTrackFavorite
        .firstOrNull()
        ?.trackFavorite
        ?.firstOrNull()
        ?.track
}

fun ShowBroadcastTrackFavoriteJoin.getFavorite(): FavoriteEntity? {
    return this.broadcastTrackFavorite
        .firstOrNull()
        ?.trackFavorite
        ?.firstOrNull()
        ?.favorite
        ?.firstOrNull()
}
