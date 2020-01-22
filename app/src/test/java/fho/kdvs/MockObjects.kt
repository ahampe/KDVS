package fho.kdvs

import fho.kdvs.global.database.*
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.topmusic.TopMusicType

object MockObjects {
    val scheduleShowsWithTimeslots: List<Pair<ShowEntity, List<TimeslotEntity>>> by lazy {
        listOf(
            Pair(
                ShowEntity(
                    id = 5235,
                    name = "Neonate--New Life",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5235,
                    timeStart = TimeHelper.makeWeekTime24h("00:00", Day.SUNDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("02:00", Day.SUNDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5238,
                    name = "Island Radio Cafe",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5238,
                    timeStart = TimeHelper.makeWeekTime24h("10:00", Day.SUNDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.SUNDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5240,
                    name = "Crossing Continents",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5240,
                    timeStart = TimeHelper.makeWeekTime24h("10:00", Day.SUNDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.SUNDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5239,
                    name = "Cross-cultural Currents",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5239,
                    timeStart = TimeHelper.makeWeekTime24h("10:00", Day.SUNDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.SUNDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5280,
                    name = "The Suicide Watch",
                    defaultImageHref = "https://goo.gl/images/nkV7oU",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5280,
                    timeStart = TimeHelper.makeWeekTime24h("22:00", Day.MONDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("01:00", Day.TUESDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5289,
                    name = "Club 903",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",

                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5289,
                    timeStart = TimeHelper.makeWeekTime24h("14:30", Day.TUESDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("16:30", Day.TUESDAY)
                    ))
            ),
            Pair(
                ShowEntity(
                    id = 5257,
                    name = "la buena onda",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5257,
                    timeStart = TimeHelper.makeWeekTime24h("10:30", Day.WEDNESDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("12:00", Day.WEDNESDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5370,
                    name = "field trip",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5370,
                    timeStart = TimeHelper.makeWeekTime24h("10:30", Day.WEDNESDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("12:00", Day.WEDNESDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5320,
                    name = "How You Jewin'?",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5320,
                    timeStart = TimeHelper.makeWeekTime24h("09:00", Day.THURSDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("09:30", Day.THURSDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5331,
                    name = "Democracy Now!",
                    defaultImageHref = "https://library.kdvs.org/media/show/images/unnamed.png",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(
                    TimeslotEntity(
                        showId= 5320,
                        timeStart = TimeHelper.makeWeekTime24h("12:00", Day.MONDAY),
                        timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.MONDAY)
                    ),
                    TimeslotEntity(
                        showId= 5320,
                        timeStart = TimeHelper.makeWeekTime24h("12:00", Day.TUESDAY),
                        timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.TUESDAY)
                    ),
                    TimeslotEntity(
                        showId= 5320,
                        timeStart = TimeHelper.makeWeekTime24h("12:00", Day.WEDNESDAY),
                        timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.WEDNESDAY)
                    ),
                    TimeslotEntity(
                        showId= 5320,
                        timeStart = TimeHelper.makeWeekTime24h("12:00", Day.THURSDAY),
                        timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.THURSDAY)
                    ),
                    TimeslotEntity(
                        showId= 5320,
                        timeStart = TimeHelper.makeWeekTime24h("12:00", Day.FRIDAY),
                        timeEnd = TimeHelper.makeWeekTime24h("13:00", Day.FRIDAY)
                    )
                )
            ),
            Pair(
                ShowEntity(
                    id = 5355,
                    name = "1000 Points of Fright",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5355,
                    timeStart = TimeHelper.makeWeekTime24h("20:00", Day.FRIDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("22:00", Day.FRIDAY)
                ))
            ),
            Pair(
                ShowEntity(
                    id = 5364,
                    name = "UnAbbreviated Country",
                    defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                    quarter = Quarter.WINTER,
                    year = 2019
                ),
                listOf(TimeslotEntity(
                    showId= 5320,
                    timeStart = TimeHelper.makeWeekTime24h("16:00", Day.SATURDAY),
                    timeEnd = TimeHelper.makeWeekTime24h("18:00", Day.SATURDAY)
                ))
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
                date = TimeHelper.makeLocalDate("2019-01-14")
            ),
            BroadcastEntity(
                broadcastId = 50770,
                showId = 5280,
                date = TimeHelper.makeLocalDate("2019-01-07")
            )
        )
    }

    val staffMembers: List<StaffEntity> by lazy {
        listOf(
            StaffEntity(
                name = "Jacob Engel",
                position = "General Manager",
                email = "gm@kdvs.org",
                duties = "Daily running of the station, FCC regulation, budget, community relations, and general overseeing",
                officeHours = "Wednesday 2:30-5pm\n" +
                        "Friday 12-3pm"
            ),
            StaffEntity(
                name = "Tania Quintana & Jay Lounds",
                position = "Co-Programming Directors",
                email = "programming@kdvs.org",
                duties = "Scheduling, training, compliance with FCC protocol, and quality control",
                officeHours = "(Tania):\n" +
                        "Monday 2-4pm\n" +
                        "Thursday 12-2pm\n" +
                        "(Jay):\n" +
                        "Thursday 2-3pm\n" +
                        "Friday 1-4pm"
            ),
            StaffEntity(
                name = "Grace Swan-Streepy & Desmond Chu",
                position = "Co-Events Directors",
                email = "events@kdvs.org",
                duties = "KDVS Presents booking and cross-promotion",
                officeHours = "(Grace):\n" +
                        "By Appointment\n" +
                        "(Desmond):\n" +
                        "By Appointment"
            )
        )
    }

    val news = listOf(
        NewsEntity(
            title = "Queer Quollaboration – PA Show Highlight",
            author = "Public Affairs",
            body = "Show: Queer Quollaboration\n" +
                    "Host: Graham\n" +
                    "Show Description: Featuring interviews with people involved in local and regional efforts to advocate and support the LGBTQ community. Topics discussed: gender norms, queer activism, mental and sexual health, queer youth, queer politics, coming out, LGBT history and so much more!\n" +
                    "Contact Info: \n" +
                    "geeverett@ucdavis.edu\n" +
                    "...",
            date = TimeHelper.makeLocalDate("2018-11-07"),
            articleHref = "https://kdvs.org/queer-quollaboration-pa-show-highlight/",
            imageHref = null
        ),
        NewsEntity(
            title = "Fundraiser Week",
            author = "Public Affairs",
            body = "Make sure to tune into our Public Affairs programs that air every week from 8:00AM-9:30AM and 4:30PM-6:00PM!...",
            date = TimeHelper.makeLocalDate("2018-11-06"),
            articleHref = "https://kdvs.org/fundraiser-week/",
            imageHref = null
        ),
        NewsEntity(
            title = "UC Davis Makes Forbe’s List for Number of Women in STEM",
            author = "General Manager",
            body = "UCD was ranked #1 on Forbe’s list as the most highly valued degree for women in Science, Technology, Engineering, and Mathematics (STEM). Our spring reporter, Myah Daniels, discusses the UC Davis grant program ADVANCE, which encourages women to establish careers in science and engineering; and interviews students in the program. (Image: ucdavis.edu)",
            date = TimeHelper.makeLocalDate("2016-09-11"),
            articleHref = "https://kdvs.org/uc-davis-makes-forbes-list-for-number-of-women-in-stem/",
            imageHref = "./Category Archive for _News_ _ KDVS_files/rose-hong-truong-uc-davis-best-women-stem-300x300.jpg"
        )
    )

    val playlist = listOf(
        TrackEntity(
            broadcastId = 51742,
            position = 0,
            artist = "Focus",
            song = "All Together...Oh That",
            album = "Mother Focus",
            label = "EMI"
        ),
        TrackEntity(
            broadcastId = 51742,
            position = 1,
            artist = "PFM",
            song = "Photos of Ghosts",
            album = "Photos of Ghosts",
            label = "RCA",
            comment = "English lang. version of Per un amico"
        ),
        TrackEntity(
            broadcastId = 51742,
            position = 2,
            airbreak = true
        ),
        TrackEntity(
            broadcastId = 51742,
            position = 3,
            artist = "Pete Sinfield",
            song = "Still",
            album = "Stillusion",
            label = "Manticore",
            comment = "ELP, PFM Lyrici"
        ),
        TrackEntity(
            broadcastId = 51742,
            position = 4,
            airbreak = true
        ),
        TrackEntity(
            broadcastId = 51742,
            position = 5,
            artist = "Eden in Progress",
            song = "The Witness",
            album = "From a single",
            label = "self released"
        )
    )

    val topAdds = listOf(
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-03-25"),
            type=TopMusicType.ADD,
            position = 0,
            artist = "Cavemen",
            album = "Lowlife EP",
            label = "Slovenly",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null

        ),
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-03-25"),
            type=TopMusicType.ADD,
            position = 2,
            artist = "Is In Unsamble",
            album = "Is The Belly/In The Belly",
            label = "Gilgongo",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null
        ),
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-02-25"),
            type=TopMusicType.ADD,
            position = 0,
            artist = "The Grundybergs",
            album = "Playing Baseball With Walt Whitman",
            label = "Self-Released",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null
        )
    )

    val topAlbums = listOf(
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-04-01"),
            type=TopMusicType.ALBUM,
            position = 18,
            artist = "Ibibio Sound Machine",
            album = "Doko Mien",
            label = "Merge",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null
        ),
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-03-25"),
            type=TopMusicType.ALBUM,
            position = 7,
            artist = "Fruit Bats & Vetiver",
            album = "In Real Life (Live At Spacebomb Studios)",
            label = "Spacebomb",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null
        ),
        TopMusicEntity(
            weekOf = TimeHelper.makeLocalDate("2019-03-04"),
            type=TopMusicType.ALBUM,
            position = 4,
            artist = "The Real Kids",
            album = "The Kids 1974 Demos - The Real Kids 1977/1978 demos/live",
            label = "Crypt",
            imageHref= null,
            spotifyAlbumUri = null,
            spotifyTrackUris = null
        )
    )

    val fundraiser = FundraiserEntity(
        goal = 50000,
        current = 47,
        dateStart = TimeHelper.makeLocalDate("2019-04-22"),
        dateEnd = TimeHelper.makeLocalDate("2019-04-28")
    )
}