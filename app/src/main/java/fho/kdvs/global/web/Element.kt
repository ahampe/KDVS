package fho.kdvs.global.web

import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

fun Element?.parseHtml(): String? {
    if (this == null) return null

    val unescaped = Parser.unescapeEntities(html().trim(), false)

    return when (unescaped.isEmpty()) {
        true -> null
        false -> unescaped
    }
}