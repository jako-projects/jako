package java.net

actual fun interface CookiePolicy {
    actual fun shouldAccept(uri: URI?, cookie: HttpCookie?): Boolean

    actual companion object {
        actual val ACCEPT_ALL: CookiePolicy = CookiePolicy { _, _ -> true }
        actual val ACCEPT_NONE: CookiePolicy = CookiePolicy { _, _ -> false }
        actual val ACCEPT_ORIGINAL_SERVER: CookiePolicy = CookiePolicy { uri, cookie ->
            if (uri == null || cookie == null) return@CookiePolicy false
            val domain = cookie.domain ?: return@CookiePolicy true
            val host = uri.host ?: return@CookiePolicy false
            HttpCookie.domainMatches(domain, host)
        }
    }
}
