package java.net

actual interface CookieStore {
    actual fun add(uri: URI?, cookie: HttpCookie)
    actual fun get(uri: URI): List<HttpCookie>
    actual fun getCookies(): List<HttpCookie>
    actual fun getURIs(): List<URI>
    actual fun remove(uri: URI?, cookie: HttpCookie): Boolean
    actual fun removeAll(): Boolean
}
