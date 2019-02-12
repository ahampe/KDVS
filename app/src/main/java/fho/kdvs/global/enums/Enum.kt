package fho.kdvs.global.enums

inline fun <reified T : Enum<T>> enumValueOrDefault(name: String?, default: T): T =
    enumValues<T>().find { it.name == name } ?: default

inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? =
    enumValues<T>().find { it.name == name }