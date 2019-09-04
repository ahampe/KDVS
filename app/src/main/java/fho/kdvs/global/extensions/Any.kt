package fho.kdvs.global.extensions

/** Helper method for determining if a generic value is null or empty. */
fun Any?.isNullOrEmptyGeneric(): Boolean = this == null || (this is Collection<*> && this.isEmpty())