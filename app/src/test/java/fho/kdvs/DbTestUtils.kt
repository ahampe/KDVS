package fho.kdvs

import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import java.text.SimpleDateFormat
import java.util.*

object DbTestUtils {
    private val formatter = SimpleDateFormat("HH:mm")
    fun createShows() = listOf(
        ShowEntity(
            id = 1888,
            name = "Pick a Bale of Cotton",
            host = "Leadbelly",
            genre = "Blues",
            defaultDesc = "Goodnight Irene",
            defaultImageHref = "http://www.leadbelly.com/cotton.jpg",
            timeStart = formatter.parse("01:00"),
            timeEnd = formatter.parse("02:00"),
            dayOfWeekStart = Day.SUNDAY,
            dayOfWeekEnd = Day.SUNDAY,
            quarter = Quarter.SPRING,
            year = 1943
        ),
        ShowEntity(
            id = 1889,
            name = "Tristram Shandy",
            host = "Laurence Sterne",
            genre = "Rabelaisian",
            defaultDesc = "The Absolute Madman",
            defaultImageHref = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Laurence_Sterne_by_Sir_Joshua_Reynolds.jpg/800px-Laurence_Sterne_by_Sir_Joshua_Reynolds.jpg",
            timeStart = formatter.parse("22:00"),
            timeEnd = formatter.parse("01:00"),
            dayOfWeekStart = Day.SATURDAY,
            dayOfWeekEnd = Day.SUNDAY,
            quarter = Quarter.SPRING,
            year = 1943
        )
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