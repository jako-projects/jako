@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect interface CookieStore {
    fun add(uri: URI?, cookie: HttpCookie)
    fun get(uri: URI): List<HttpCookie>
    fun getCookies(): List<HttpCookie>
    fun getURIs(): List<URI>
    fun remove(uri: URI?, cookie: HttpCookie): Boolean
    fun removeAll(): Boolean
}
