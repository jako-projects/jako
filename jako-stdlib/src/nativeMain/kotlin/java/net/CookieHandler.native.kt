package java.net

actual abstract class CookieHandler actual constructor() {
    actual abstract fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>>
    actual abstract fun put(uri: URI, responseHeaders: Map<String, List<String>>)

    actual companion object {
        private var cookieHandler: CookieHandler? = null

        actual fun getDefault(): CookieHandler? = cookieHandler

        actual fun setDefault(cHandler: CookieHandler?) {
            cookieHandler = cHandler
        }
    }
}
