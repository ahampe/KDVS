package fho.kdvs

import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity

object MockObjects {
    val scheduleShows: List<ShowEntity> by lazy {
        listOf(
            ShowEntity(
                id = 5235,
                name = "Neonate--New Life",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("00:00"),
                timeEnd = TestUtils.makeDateFromTime("02:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5238,
                name = "Island Radio Cafe",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5240,
                name = "Crossing Continents",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5239,
                name = "Cross-cultural Currents",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5280,
                name = "The Suicide Watch",
                defaultImageHref = "https://goo.gl/images/nkV7oU",
                timeStart = TestUtils.makeDateFromTime("22:00"),
                timeEnd = TestUtils.makeDateFromTime("01:00"),
                dayOfWeek = Day.MONDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5289,
                name = "Club 903",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("14:30"),
                timeEnd = TestUtils.makeDateFromTime("16:30"),
                dayOfWeek = Day.TUESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5257,
                name = "la buena onda",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:30"),
                timeEnd = TestUtils.makeDateFromTime("12:00"),
                dayOfWeek = Day.WEDNESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5370,
                name = "field trip",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:30"),
                timeEnd = TestUtils.makeDateFromTime("12:00"),
                dayOfWeek = Day.WEDNESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5320,
                name = "How You Jewin'?",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("09:00"),
                timeEnd = TestUtils.makeDateFromTime("09:30"),
                dayOfWeek = Day.THURSDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5331,
                name = "Democracy Now!",
                defaultImageHref = "https://library.kdvs.org/media/show/images/unnamed.png",
                timeStart = TestUtils.makeDateFromTime("12:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.THURSDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5333,
                name = "Democracy Now!",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("12:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.FRIDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5355,
                name = "1000 Points of Fright",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("20:00"),
                timeEnd = TestUtils.makeDateFromTime("22:00"),
                dayOfWeek = Day.FRIDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5364,
                name = "UnAbbreviated Country",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("16:00"),
                timeEnd = TestUtils.makeDateFromTime("18:00"),
                dayOfWeek = Day.SATURDAY,
                quarter = Quarter.WINTER,
                year = 2019
            )
        )
    }

    val showDetails: List<ShowEntity> by lazy {
        listOf(
            ShowEntity(
                id = 5235,
                host = "Punk Roge",
                genre = "Hardcore, Old School Punk, Punk, Street Punk",
                defaultDesc = "2 Hours of punk rock."
            ),
            ShowEntity(
                id = 5238,
                host = "Gary B. Goode",
                genre = "Reggae",
                defaultDesc = "New releases plus surprises. Reggae ten at ten. Latino/a, Hawaiian music. African as well. Jazz too."
            ),
            ShowEntity(
                id = 5240,
                host = "Gil Medovoy",
                genre = "International",
                defaultDesc = "International/World"
            ),
            ShowEntity(
                id = 5239,
                host = "Mindy",
                genre = "International, Reggae",
                defaultDesc = "Reggae + African"
            ),
            ShowEntity(
                id = 5280,
                host = "Ophelia Necro",
                genre = ""
            ),
            ShowEntity(
                id = 5289,
                host = "HIV",
                genre = "Electronic",
                defaultDesc = "FKA The Swear Jar."
            ),
            ShowEntity(
                id = 5257,
                host = "dj pan francés",
                genre = "Latino Alternativo",
                defaultDesc = "reminder to stop sleeping on latin american rock."
            ),
            ShowEntity(
                id = 5370,
                host = "crimson wave & Mood Ring",
                genre = "Eclectic",
                defaultDesc = "i made you a mixtape!"
            ),
            ShowEntity(
                id = 5320,
                host = "Mathilda",
                genre = "Public Affairs",
                defaultDesc = "Chat show about Jewish life + culture"
            ),
            ShowEntity(
                id = 5331,
                host = "Staff",
                genre = "News"
            ),
            ShowEntity(
                id = 5333,
                host = "Staff",
                genre = "News"
            ),
            ShowEntity(
                id = 5355,
                host = "Pirate of the High Frequenseas",
                genre = "Metal",
                defaultDesc = "Metal and all of it's ugly children"
            ),
            ShowEntity(
                id = 5364,
                host = "Fuzzy Mic",
                genre = "Bluegrass, Blues, Country, Folk",
                defaultDesc = "Music from the country is a big world. And we'll listen to all of it. Country, bluegrass, folk, and blues all come from the same roots. Tune in and hear modern, classic, and roots country."
            )
        )
    }

    val broadcasts: List<BroadcastEntity> by lazy {
        listOf(
            BroadcastEntity(
                broadcastId = 50771,
                showId = 5280,
                date = TestUtils.makeDate("01/14/2019")
            ),
            BroadcastEntity(
                broadcastId = 50770,
                showId = 5280,
                date = TestUtils.makeDate("01/07/2019")
            )
        )
    }

    val playlist = listOf(
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 0,
            artist = "Focus",
            song = "All Together...Oh That",
            album = "Mother Focus",
            label = "EMI"
        ),
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 1,
            artist = "PFM",
            song = "Photos of Ghosts",
            album = "Photos of Ghosts",
            label = "RCA",
            comment = "English lang. version of Per un amico"
        ),
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 2,
            airbreak = true
        ),
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 3,
            artist = "Pete Sinfield",
            song = "Still",
            album = "Stillusion",
            label = "Manticore",
            comment = "ELP, PFM Lyrici"
        ),
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 4,
            airbreak = true
        ),
        TrackEntity(
            trackId = 0,
            broadcastId = 51742,
            position = 5,
            artist = "Eden in Progress",
            song = "The Witness",
            album = "From a single",
            label = "self released"
        )
    )
}