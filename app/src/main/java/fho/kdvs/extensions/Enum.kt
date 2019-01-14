package fho.kdvs.extensions

inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T) =
    enumValues<T>().find { it.name == name } ?: default