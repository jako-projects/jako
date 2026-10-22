@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class CookieManager : CookieHandler {
    constructor()
    constructor(store: CookieStore?, cookiePolicy: CookiePolicy?)

    open fun setCookiePolicy(cookiePolicy: CookiePolicy?)
    open fun getCookieStore(): CookieStore
    open val cookieStore: CookieStore
    override fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>>
    override fun put(uri: URI, responseHeaders: Map<String, List<String>>)
}
