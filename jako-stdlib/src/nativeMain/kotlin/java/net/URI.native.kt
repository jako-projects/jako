package java.net

import java.io.Serializable

actual class URI actual constructor(
    private val str: String
) : Serializable, Comparable<URI> {

    private var schemeStr: String? = null
    private var authorityStr: String? = null
    private var pathStr: String? = null
    private var queryStr: String? = null
    private var fragmentStr: String? = null

    init {
        parse()
    }

    actual constructor(
        scheme: String?,
        authority: String?,
        path: String?,
        query: String?,
        fragment: String?
    ) : this(buildString(scheme, authority, path, query, fragment))

    private fun parse() {
        for (i in 0 until str.length) {
            val c = str[i]
            if (c.code in 0..0x1F || c.code in 0x7F..0x9F || c.isWhitespace() || c == ' ' || c == '\u2009' || c == '\u3000' || c == '\u00A0') {
                throw URISyntaxException(str, "Illegal character in URI at index $i: $c")
            }
        }

        var s = str
        val hashIdx = s.indexOf('#')
        if (hashIdx >= 0) {
            fragmentStr = s.substring(hashIdx + 1)
            s = s.substring(0, hashIdx)
        }

        val qIdx = s.indexOf('?')
        if (qIdx >= 0) {
            queryStr = s.substring(qIdx + 1)
            s = s.substring(0, qIdx)
        }

        val colonIdx = s.indexOf(':')
        val slashIdx = s.indexOf('/')
        if (colonIdx >= 0 && (slashIdx < 0 || colonIdx < slashIdx)) {
            schemeStr = s.substring(0, colonIdx)
            s = s.substring(colonIdx + 1)
        }

        if (s.startsWith("//")) {
            val nextSlash = s.indexOf('/', 2)
            if (nextSlash >= 0) {
                val auth = s.substring(2, nextSlash)
                authorityStr = if (auth.isEmpty()) null else auth
                pathStr = s.substring(nextSlash)
            } else {
                val auth = s.substring(2)
                authorityStr = if (auth.isEmpty()) null else auth
                pathStr = ""
            }
        } else {
            pathStr = s
        }
    }

    actual fun getScheme(): String? = schemeStr
    actual fun getAuthority(): String? = authorityStr
    actual fun getPath(): String? = pathStr
    actual fun getQuery(): String? = queryStr
    actual fun getFragment(): String? = fragmentStr

    actual val scheme: String? get() = getScheme()
    actual val authority: String? get() = getAuthority()
    actual val path: String? get() = getPath()
    actual val query: String? get() = getQuery()
    actual val fragment: String? get() = getFragment()
    actual val host: String? get() = getHost()
    actual val port: Int get() = getPort()

    actual fun getHost(): String? {
        val auth = authorityStr ?: return null
        val atIdx = auth.indexOf('@')
        val hostPort = if (atIdx >= 0) auth.substring(atIdx + 1) else auth
        val colonIdx = hostPort.indexOf(':')
        val rawHost = if (colonIdx >= 0) hostPort.substring(0, colonIdx) else hostPort
        if (rawHost.isEmpty()) return null
        for (c in rawHost) {
            if (c !in 'a'..'z' && c !in 'A'..'Z' && c !in '0'..'9' && c != '-' && c != '.' && c != '_' && c != '[' && c != ']' && c != '%') {
                return null
            }
        }
        return rawHost
    }

    actual fun getPort(): Int {
        val auth = authorityStr ?: return -1
        val colonIdx = auth.lastIndexOf(':')
        if (colonIdx < 0) return -1
        return try {
            auth.substring(colonIdx + 1).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    actual fun isAbsolute(): Boolean = schemeStr != null

    actual fun isOpaque(): Boolean = isAbsolute() && (pathStr == null || !pathStr!!.startsWith("/"))

    actual fun resolve(str: String): URI = resolve(URI(str))

    actual fun resolve(uri: URI): URI {
        if (uri.isAbsolute()) return uri
        val relPath = uri.getPath() ?: ""
        val newPath = if (relPath.startsWith("/")) {
            relPath
        } else {
            val baseP = pathStr ?: ""
            val lastSlash = baseP.lastIndexOf('/')
            if (lastSlash >= 0) {
                baseP.substring(0, lastSlash + 1) + relPath
            } else {
                relPath
            }
        }
        return URI(schemeStr, authorityStr, newPath, uri.getQuery(), uri.getFragment())
    }

    actual fun toURL(): URL = URL(toString())

    actual override fun compareTo(other: URI): Int = str.compareTo(other.str)

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is URI) return false
        return str == other.str
    }

    actual override fun hashCode(): Int = str.hashCode()

    actual override fun toString(): String = str

    actual companion object {
        actual fun create(str: String): URI = URI(str)

        private fun buildString(
            scheme: String?,
            authority: String?,
            path: String?,
            query: String?,
            fragment: String?
        ): String = buildString {
            if (scheme != null) append("$scheme:")
            if (authority != null) append("//$authority")
            if (path != null) append(path)
            if (query != null) append("?$query")
            if (fragment != null) append("#$fragment")
        }
    }
}
