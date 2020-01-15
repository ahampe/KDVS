package fho.kdvs.global.database

import fho.kdvs.global.enums.Quarter

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