@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect abstract class CookieHandler {
    constructor()

    abstract fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>>
    abstract fun put(uri: URI, responseHeaders: Map<String, List<String>>)

    companion object {
        fun getDefault(): CookieHandler?
        fun setDefault(cHandler: CookieHandler?)
    }
}
