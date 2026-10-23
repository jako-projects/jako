@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect fun interface CookiePolicy {
    fun shouldAccept(uri: URI?, cookie: HttpCookie?): Boolean

    companion object {
        val ACCEPT_ALL: CookiePolicy
        val ACCEPT_NONE: CookiePolicy
        val ACCEPT_ORIGINAL_SERVER: CookiePolicy
    }
}
