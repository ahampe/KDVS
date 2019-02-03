package fho.kdvs.global.extensions

/** Convenience function for generating a list with [size] filled with nulls. */
fun listOfNulls(size: Int) = List(size) { null }

/** Extra list destructuring beyond 5 for scraper */
operator fun <T> List<T>.component6(): T {
    return get(6)
}

operator fun <T> List<T>.component7(): T {
    return get(7)
}