package fho.kdvs.global.web

import org.json.JSONObject

object JsonHelper {
    inline fun <reified T> getRootLevelElmOfType(key: String, json: JSONObject?): T?{
        var elm: T? = null

        if (json?.has(key) == true && json.get(key) is T && json.get(key) != null)
            elm = json.get(key) as? T

        return elm
    }
}