package fho.kdvs.global.database

import fho.kdvs.global.enums.Quarter

/**
 * Abstraction of a [ShowEntity] to bind [ShowEntity] and [ShowTimeslotEntity] to the same model.
 * May be used generically for handling non-time data of a show.
 * */
abstract class Show {
    abstract val id: Int
    abstract var name: String?
    abstract var host: String?
    abstract var genre: String?
    abstract var defaultDesc: String?
    abstract var defaultImageHref: String?
    abstract var quarter: Quarter?
    abstract var year: Int?
}