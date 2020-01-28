package fho.kdvs.global.export

/**
 * Interface for services requiring a login to some platform.
 * */
interface Loginable {
    fun loginIfNecessary()
}