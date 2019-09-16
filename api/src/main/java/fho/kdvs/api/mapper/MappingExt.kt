package fho.kdvs.api.mapper

fun String?.nullIfBlank(): String? = if (this.isNullOrBlank()) null else this
