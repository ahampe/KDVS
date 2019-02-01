package fho.kdvs

import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import java.util.*

object DbTestUtils {
    fun createShow() = ShowEntity(
        id = 1888,
        name = "Pick a Bale of Cotton",
        host = "Leadbelly",
        genre = "Blues",
        defaultDesc = "Goodnight Irene",
        defaultImageHref = "http://www.leadbelly.com/cotton.jpg",
        timeStart = Date(1_000L),
        timeEnd = Date(2_000L),
        dayOfWeek = Day.SUNDAY,
        quarter = Quarter.SPRING,
        year = 1943
    )

    fun createBroadcasts() = listOf(
        BroadcastEntity(
            broadcastId = 290,
            showId = 1888,
            descr = "A stompin' good time with Blind Lemon",
            date = Date(5_000_00L),
            imageHref = "http://www.leadbelly.com/blindlemon.jpg"
        ),
        BroadcastEntity(
            broadcastId = 291,
            showId = 1888,
            descr = "The Great Machine",
            date = Date(5_000_01L),
            imageHref = "http://sfprod.shikadi.net/pic/timss.png"
        )
    )

    fun createTracks() = listOf(
        TrackEntity(
            trackId = 1,
            broadcastId = 290,
            position = 0,
            artist = "The Limeliters",
            song = "Country Music's Got A Way of Making Me Feel Free",
            album = "Singing For The Fun"
        ),
        TrackEntity(
            trackId = 2,
            broadcastId = 290,
            position = 1,
            airbreak = true
        ),
        TrackEntity(
            trackId = 3,
            broadcastId = 290,
            position = 2,
            artist = "Byrd, Jonathan",
            song = "Tractor Pull",
            album = "Jonathan Byrd & The Pickup Cowboys",
            label = "Waterbug"
        ),
        TrackEntity(
            trackId = 4,
            broadcastId = 290,
            position = 3,
            artist = "Dolly Parton",
            song = "Blue Smoke",
            album = "Blue Smoke"
        )
    )
}