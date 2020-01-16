package fho.kdvs.global.database

import androidx.room.Embedded
import androidx.room.Relation
import fho.kdvs.favorite.broadcast.FavoriteBroadcastJoin
import fho.kdvs.favorite.track.FavoriteTrackJoin

open class Join

/**
 * Captures the one-to-many relation between [ShowEntity]s and their associated [TimeslotEntity]s.
 * Use in cases in which one needs to refer to all timeslots of a show.
 * (e.g. 'Democracy Now on Monday-Friday 3PM')
 *
 * [ShowTimeslotEntity] captures a specific show and timeslot relation.
 * */
class ShowTimeslotsJoin: Join() {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId", entity = TimeslotEntity::class)
    var timeslots: List<TimeslotEntity> = ArrayList()

    override fun equals(other: Any?): Boolean {
        val otherJoin = other as? ShowTimeslotsJoin
        return this.show == otherJoin?.show
                && this.timeslots == otherJoin?.timeslots
    }
}

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

class BroadcastFavoriteJoin: Join() {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(parentColumn = "broadcastId", entityColumn = "broadcastId")
    var favorite: List<FavoriteBroadcastEntity> = ArrayList()
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

class BroadcastTrackFavoriteJoin {
    @Embedded
    var broadcast: BroadcastEntity? = null

    @Relation(
        parentColumn = "broadcastId",
        entityColumn = "broadcastId",
        entity = TrackEntity::class
    )
    var trackFavorite: List<TrackFavoriteJoin> = ArrayList()
}

class TrackFavoriteJoin {
    @Embedded
    var track: TrackEntity? = null

    @Relation(parentColumn = "trackId", entityColumn = "trackId")
    var favorite: List<FavoriteTrackEntity> = ArrayList()
}

class ShowBroadcastJoin {
    @Embedded
    var show: ShowEntity? = null

    @Relation(parentColumn = "id", entityColumn = "showId")
    var broadcast: List<BroadcastEntity> = ArrayList()
}

class ShowTimeslotBroadcastJoin {
    @Embedded
    var show: ShowTimeslotEntity? = null

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

/**
 * Helper functions to return specific data types from [ShowBroadcastTrackFavoriteJoin].
 * */
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