package fho.kdvs.extensions

import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

fun Element.parseHtml() = Parser.unescapeEntities(html().trim(), false)