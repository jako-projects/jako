package java.net

import java.util.Collections

internal class InMemoryCookieStore : CookieStore {
    private val allCookies = mutableListOf<HttpCookie>()
    private val uriMap = mutableMapOf<URI, MutableList<HttpCookie>>()

    private fun baseUri(uri: URI): URI {
        return try {
            URI(uri.scheme ?: "http", uri.host ?: "", null, null, null)
        } catch (_: URISyntaxException) {
            uri
        }
    }

    private fun purgeExpired() {
        allCookies.removeAll { it.hasExpired() }
        val iter = uriMap.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            entry.value.removeAll { it.hasExpired() }
            if (entry.value.isEmpty()) iter.remove()
        }
    }

    override fun add(uri: URI?, cookie: HttpCookie) {
        allCookies.remove(cookie)
        if (cookie.maxAge != 0L) {
            allCookies.add(cookie)
            if (uri != null) {
                val bUri = baseUri(uri)
                val list = uriMap.getOrPut(bUri) { mutableListOf() }
                list.remove(cookie)
                list.add(cookie)
            }
        }
    }

    override fun get(uri: URI): List<HttpCookie> {
        purgeExpired()
        val result = mutableListOf<HttpCookie>()
        val host = uri.host
        val isSecureUri = "https".equals(uri.scheme, ignoreCase = true)
        for (c in allCookies) {
            if (c.secure && !isSecureUri) continue
            val d = c.domain
            if (d != null && host != null && HttpCookie.domainMatches(d, host)) {
                if (!result.contains(c)) result.add(c)
            }
        }
        val bUri = baseUri(uri)
        val direct = uriMap[bUri]
        if (direct != null) {
            for (c in direct) {
                if (c.secure && !isSecureUri) continue
                if (!result.contains(c)) result.add(c)
            }
        }
        return Collections.unmodifiableList(result)
    }

    override fun getCookies(): List<HttpCookie> {
        purgeExpired()
        return Collections.unmodifiableList(allCookies.toList())
    }

    override fun getURIs(): List<URI> {
        purgeExpired()
        return Collections.unmodifiableList(uriMap.keys.toList())
    }

    override fun remove(uri: URI?, cookie: HttpCookie): Boolean {
        val removed = allCookies.remove(cookie)
        if (uri != null) {
            val bUri = baseUri(uri)
            uriMap[bUri]?.remove(cookie)
        }
        return removed
    }

    override fun removeAll(): Boolean {
        if (allCookies.isEmpty()) return false
        allCookies.clear()
        uriMap.clear()
        return true
    }
}

actual open class CookieManager actual constructor(
    store: CookieStore?,
    cookiePolicy: CookiePolicy?
) : CookieHandler() {
    private val cookieJar: CookieStore = store ?: InMemoryCookieStore()
    private var policyCallback: CookiePolicy = cookiePolicy ?: CookiePolicy.ACCEPT_ORIGINAL_SERVER

    actual constructor() : this(null, null)

    actual open fun setCookiePolicy(cookiePolicy: CookiePolicy?) {
        if (cookiePolicy != null) {
            policyCallback = cookiePolicy
        }
    }

    actual open fun getCookieStore(): CookieStore = cookieJar

    actual open val cookieStore: CookieStore
        get() = cookieJar

    actual override fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>> {
        val path = uri.path.takeUnless { it.isNullOrEmpty() } ?: "/"
        val cookies = mutableListOf<HttpCookie>()
        for (cookie in cookieJar.get(uri)) {
            val cookiePath = cookie.path ?: "/"
            if (path.startsWith(cookiePath)) {
                cookies.add(cookie)
            }
        }
        val map = mutableMapOf<String, List<String>>()
        if (cookies.isNotEmpty()) {
            map["Cookie"] = cookies.map { it.toString() }
        }
        return Collections.unmodifiableMap(map)
    }

    actual override fun put(uri: URI, responseHeaders: Map<String, List<String>>) {
        for ((headerKey, headerValues) in responseHeaders) {
            if (headerKey == null) continue
            if (!headerKey.equals("Set-Cookie", ignoreCase = true) &&
                !headerKey.equals("Set-Cookie2", ignoreCase = true)
            ) {
                continue
            }
            for (headerValue in headerValues) {
                val cookies = try {
                    HttpCookie.parse(headerValue)
                } catch (_: IllegalArgumentException) {
                    emptyList()
                }
                for (cookie in cookies) {
                    if (cookie.path == null) {
                        val path = uri.path ?: "/"
                        val lastSlash = path.lastIndexOf('/')
                        cookie.path = if (lastSlash > 0) path.substring(0, lastSlash + 1) else "/"
                    }
                    if (cookie.domain == null) {
                        cookie.domain = uri.host
                    }
                    if (policyCallback.shouldAccept(uri, cookie)) {
                        cookieJar.add(uri, cookie)
                    }
                }
            }
        }
    }
}
