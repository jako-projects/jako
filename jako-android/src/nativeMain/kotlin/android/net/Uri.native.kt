package android.net

import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.Locale

actual abstract class Uri : Parcelable, Comparable<Uri> {
    actual abstract val isHierarchical: Boolean
    actual open val isOpaque: Boolean get() = !isHierarchical
    actual abstract val isRelative: Boolean
    actual open val isAbsolute: Boolean get() = !isRelative
    actual abstract val scheme: String?
    actual abstract val schemeSpecificPart: String
    actual abstract val encodedSchemeSpecificPart: String
    actual abstract val authority: String?
    actual abstract val encodedAuthority: String?
    actual abstract val userInfo: String?
    actual abstract val encodedUserInfo: String?
    actual abstract val host: String?
    actual abstract val port: Int
    actual abstract val path: String?
    actual abstract val encodedPath: String?
    actual abstract val query: String?
    actual abstract val encodedQuery: String?
    actual abstract val fragment: String?
    actual abstract val encodedFragment: String?
    actual abstract val pathSegments: List<String>
    actual abstract val lastPathSegment: String?

    actual open val queryParameterNames: Set<String>
        get() = getQueryParameterNames()

    actual abstract fun isHierarchical(): Boolean
    actual open fun isOpaque(): Boolean = !isHierarchical()
    actual abstract fun isRelative(): Boolean
    actual open fun isAbsolute(): Boolean = !isRelative()
    actual abstract fun getScheme(): String?
    actual abstract fun getSchemeSpecificPart(): String
    actual abstract fun getEncodedSchemeSpecificPart(): String
    actual abstract fun getAuthority(): String?
    actual abstract fun getEncodedAuthority(): String?
    actual abstract fun getUserInfo(): String?
    actual abstract fun getEncodedUserInfo(): String?
    actual abstract fun getHost(): String?
    actual abstract fun getPort(): Int
    actual abstract fun getPath(): String?
    actual abstract fun getEncodedPath(): String?
    actual abstract fun getQuery(): String?
    actual abstract fun getEncodedQuery(): String?
    actual abstract fun getFragment(): String?
    actual abstract fun getEncodedFragment(): String?
    actual abstract fun getPathSegments(): List<String>
    actual abstract fun getLastPathSegment(): String?

    actual open fun getQueryParameterNames(): Set<String> {
        if (isOpaque()) {
            throw UnsupportedOperationException("This isn't a hierarchical URI.")
        }
        val query = getEncodedQuery() ?: return emptySet()
        val names = LinkedHashSet<String>()
        var start = 0
        do {
            val next = query.indexOf('&', start)
            val end = if (next == -1) query.length else next
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            val name = query.substring(start, separator)
            names.add(decode(name) ?: "")
            start = end + 1
        } while (start < query.length)
        return Collections.unmodifiableSet(names)
    }

    actual open fun getQueryParameters(key: String): List<String> {
        if (isOpaque()) {
            throw UnsupportedOperationException("This isn't a hierarchical URI.")
        }
        val query = getEncodedQuery() ?: return emptyList()
        val encodedKey = encode(key) ?: return emptyList()
        val values = ArrayList<String>()
        var start = 0
        do {
            val nextAmpersand = query.indexOf('&', start)
            val end = if (nextAmpersand != -1) nextAmpersand else query.length
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            if (separator - start == encodedKey.length &&
                query.regionMatches(start, encodedKey, 0, encodedKey.length)
            ) {
                if (separator == end) {
                    values.add("")
                } else {
                    values.add(decode(query.substring(separator + 1, end)) ?: "")
                }
            }
            if (nextAmpersand != -1) {
                start = nextAmpersand + 1
            } else {
                break
            }
        } while (true)
        return Collections.unmodifiableList(values)
    }

    actual open fun getQueryParameter(key: String): String? {
        if (isOpaque()) {
            throw UnsupportedOperationException("This isn't a hierarchical URI.")
        }
        val query = getEncodedQuery() ?: return null
        val encodedKey = encode(key, null) ?: return null
        val length = query.length
        var start = 0
        while (true) {
            val nextAmpersand = query.indexOf('&', start)
            val end = if (nextAmpersand != -1) nextAmpersand else length
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            if (separator - start == encodedKey.length &&
                query.regionMatches(start, encodedKey, 0, encodedKey.length)
            ) {
                return if (separator == end) {
                    ""
                } else {
                    val encodedValue = query.substring(separator + 1, end)
                    decode(encodedValue, true)
                }
            }
            if (nextAmpersand != -1) {
                start = nextAmpersand + 1
            } else {
                break
            }
        }
        return null
    }

    actual open fun getBooleanQueryParameter(key: String, defaultValue: Boolean): Boolean {
        val flag = getQueryParameter(key) ?: return defaultValue
        val lower = flag.lowercase(Locale.ROOT)
        return "false" != lower && "0" != lower
    }

    actual open fun normalizeScheme(): Uri {
        val scheme = getScheme() ?: return this
        val lowerScheme = scheme.lowercase(Locale.ROOT)
        if (scheme == lowerScheme) return this
        return buildUpon().scheme(lowerScheme).build()
    }

    actual abstract fun buildUpon(): Builder

    actual override fun equals(other: Any?): Boolean {
        if (other !is Uri) return false
        return toString() == other.toString()
    }

    actual override fun hashCode(): Int = toString().hashCode()

    actual override fun compareTo(other: Uri): Int = toString().compareTo(other.toString())

    actual abstract override fun toString(): String

    actual final class Builder actual constructor() {
        private var scheme: String? = null
        private var encodedOpaquePart: String? = null
        private var encodedAuthority: String? = null
        private var encodedPath: String? = null
        private var encodedQuery: String? = null
        private var encodedFragment: String? = null

        actual fun scheme(scheme: String?): Builder {
            this.scheme = scheme
            return this
        }

        actual fun opaquePart(opaquePart: String?): Builder {
            this.encodedOpaquePart = encode(opaquePart, null)
            return this
        }

        actual fun encodedOpaquePart(opaquePart: String?): Builder {
            this.encodedOpaquePart = opaquePart
            return this
        }

        actual fun authority(authority: String?): Builder {
            this.encodedOpaquePart = null
            this.encodedAuthority = encode(authority, "@[]!$&'()*+,;=:.-_~")
            return this
        }

        actual fun encodedAuthority(authority: String?): Builder {
            this.encodedOpaquePart = null
            this.encodedAuthority = authority
            return this
        }

        actual fun path(path: String?): Builder {
            this.encodedOpaquePart = null
            if (path == null) {
                this.encodedPath = null
            } else {
                this.encodedPath = encodePath(path)
            }
            return this
        }

        actual fun encodedPath(path: String?): Builder {
            this.encodedOpaquePart = null
            this.encodedPath = path
            return this
        }

        actual fun appendPath(newSegment: String): Builder {
            return appendEncodedPath(encode(newSegment) ?: "")
        }

        actual fun appendEncodedPath(newSegment: String): Builder {
            this.encodedOpaquePart = null
            val current = encodedPath
            if (current == null || current.isEmpty()) {
                encodedPath = "/$newSegment"
            } else if (current.endsWith("/")) {
                encodedPath = current + newSegment
            } else {
                encodedPath = "$current/$newSegment"
            }
            return this
        }

        actual fun query(query: String?): Builder {
            this.encodedOpaquePart = null
            this.encodedQuery = encode(query, null)
            return this
        }

        actual fun encodedQuery(query: String?): Builder {
            this.encodedOpaquePart = null
            this.encodedQuery = query
            return this
        }

        actual fun fragment(fragment: String?): Builder {
            this.encodedFragment = encode(fragment, null)
            return this
        }

        actual fun encodedFragment(fragment: String?): Builder {
            this.encodedFragment = fragment
            return this
        }

        actual fun appendQueryParameter(key: String, value: String?): Builder {
            this.encodedOpaquePart = null
            val encodedPair = "${encode(key, null)}=${encode(value, null)}"
            val current = encodedQuery
            if (current == null || current.isEmpty()) {
                encodedQuery = encodedPair
            } else {
                encodedQuery = "$current&$encodedPair"
            }
            return this
        }

        actual fun clearQuery(): Builder {
            this.encodedQuery = null
            return this
        }

        actual fun build(): Uri {
            if (encodedOpaquePart != null) {
                val s = scheme ?: throw UnsupportedOperationException("An opaque URI must have a scheme.")
                val sb = StringBuilder()
                sb.append(s).append(':').append(encodedOpaquePart)
                if (encodedFragment != null) {
                    sb.append('#').append(encodedFragment)
                }
                return StringUri(sb.toString())
            } else {
                val sb = StringBuilder()
                if (scheme != null) {
                    sb.append(scheme).append(':')
                }
                if (encodedAuthority != null) {
                    sb.append("//").append(encodedAuthority)
                }
                val p = encodedPath
                if (p != null) {
                    if (encodedAuthority != null && p.isNotEmpty() && !p.startsWith("/")) {
                        sb.append('/')
                    }
                    sb.append(p)
                }
                if (encodedQuery != null) {
                    sb.append('?').append(encodedQuery)
                }
                if (encodedFragment != null) {
                    sb.append('#').append(encodedFragment)
                }
                return StringUri(sb.toString())
            }
        }

        actual override fun toString(): String = build().toString()
    }

    actual companion object {
        actual val EMPTY: Uri = StringUri("")
        actual val CREATOR: Parcelable.Creator<Uri> = object : Parcelable.Creator<Uri> {
            override fun createFromParcel(source: Parcel): Uri {
                val str = source.readString() ?: ""
                return if (str.isEmpty()) EMPTY else StringUri(str)
            }

            override fun newArray(size: Int): Array<Uri?> = arrayOfNulls(size)
        }

        private const val DEFAULT_ALLOWED = "_-!.~'()*"
        private const val HEX_DIGITS = "0123456789ABCDEF"

        actual fun parse(uriString: String): Uri = StringUri(uriString)

        actual fun fromFile(file: File): Uri {
            val path = encodePath(file.absolutePath)
            val normalizedPath = if (path.startsWith("/")) path else "/$path"
            return StringUri("file://$normalizedPath")
        }

        actual fun fromParts(scheme: String, ssp: String, fragment: String?): Uri {
            val sb = StringBuilder()
            sb.append(scheme).append(':').append(encode(ssp, null))
            if (fragment != null) {
                sb.append('#').append(encode(fragment, null))
            }
            return StringUri(sb.toString())
        }

        actual fun encode(s: String?): String? = encode(s, null)

        actual fun encode(s: String?, allow: String?): String? {
            if (s == null) return null
            var encoded: StringBuilder? = null
            val oldLength = s.length
            var current = 0
            while (current < oldLength) {
                var nextToEncode = current
                while (nextToEncode < oldLength && isAllowed(s[nextToEncode], allow)) {
                    nextToEncode++
                }
                if (nextToEncode == oldLength) {
                    return if (current == 0) {
                        s
                    } else {
                        encoded!!.append(s, current, oldLength)
                        encoded.toString()
                    }
                }
                if (encoded == null) {
                    encoded = StringBuilder()
                }
                if (nextToEncode > current) {
                    encoded.append(s, current, nextToEncode)
                }
                current = nextToEncode
                var nextAllowed = current + 1
                while (nextAllowed < oldLength && !isAllowed(s[nextAllowed], allow)) {
                    nextAllowed++
                }
                val toEncode = s.substring(current, nextAllowed)
                val bytes = toEncode.toByteArray(StandardCharsets.UTF_8)
                for (b in bytes) {
                    val v = b.toInt() and 0xFF
                    encoded.append('%')
                    encoded.append(HEX_DIGITS[v ushr 4])
                    encoded.append(HEX_DIGITS[v and 0x0F])
                }
                current = nextAllowed
            }
            return encoded?.toString() ?: s
        }

        private fun isAllowed(c: Char, allow: String?): Boolean {
            return (c in 'A'..'Z') ||
                (c in 'a'..'z') ||
                (c in '0'..'9') ||
                DEFAULT_ALLOWED.indexOf(c) != -1 ||
                (allow != null && allow.indexOf(c) != -1)
        }

        internal fun encodePath(path: String): String {
            return encode(path, "/") ?: ""
        }

        actual fun decode(s: String?): String? = decode(s, false)

        internal fun decode(s: String?, convertPlus: Boolean): String? {
            if (s == null) return null
            val len = s.length
            if (len == 0) return s
            if (s.indexOf('%') < 0 && (!convertPlus || s.indexOf('+') < 0)) {
                return s
            }
            val sb = StringBuilder(len)
            val byteBuffer = ArrayList<Byte>()
            var i = 0
            while (i < len) {
                val c = s[i]
                if (c == '%') {
                    byteBuffer.clear()
                    while (i < len && s[i] == '%') {
                        if (i + 2 < len) {
                            val d1 = hexValue(s[i + 1])
                            val d2 = hexValue(s[i + 2])
                            if (d1 >= 0 && d2 >= 0) {
                                byteBuffer.add(((d1 shl 4) or d2).toByte())
                                i += 3
                                continue
                            }
                        }
                        // Invalid % sequence: replacement char U+FFFD in UTF-8
                        byteBuffer.add(0xEF.toByte())
                        byteBuffer.add(0xBF.toByte())
                        byteBuffer.add(0xBD.toByte())
                        i++
                        break
                    }
                    if (byteBuffer.isNotEmpty()) {
                        sb.append(String(byteBuffer.toByteArray(), StandardCharsets.UTF_8))
                    }
                } else if (convertPlus && c == '+') {
                    sb.append(' ')
                    i++
                } else {
                    sb.append(c)
                    i++
                }
            }
            return sb.toString()
        }

        private fun hexValue(c: Char): Int = when (c) {
            in '0'..'9' -> c - '0'
            in 'a'..'f' -> c - 'a' + 10
            in 'A'..'F' -> c - 'A' + 10
            else -> -1
        }

        actual fun withAppendedPath(baseUri: Uri, pathSegment: String): Uri {
            return baseUri.buildUpon().appendEncodedPath(pathSegment).build()
        }
    }
}

private class StringUri(private val uriString: String) : Uri() {
    private val ssi: Int = findSchemeSeparator()
    private val fsi: Int = findFragmentSeparator()

    private fun findSchemeSeparator(): Int {
        val colon = uriString.indexOf(':')
        val frag = uriString.indexOf('#')
        return if (frag != -1 && colon > frag) -1 else colon
    }

    private fun findFragmentSeparator(): Int {
        return uriString.indexOf('#', ssi)
    }

    override val isHierarchical: Boolean
        get() = isHierarchical()

    override fun isHierarchical(): Boolean {
        if (ssi == -1) return true
        if (uriString.length == ssi + 1) return false
        return uriString[ssi + 1] == '/'
    }

    override val isRelative: Boolean
        get() = isRelative()

    override fun isRelative(): Boolean = ssi == -1

    override val scheme: String?
        get() = getScheme()

    override fun getScheme(): String? {
        return if (ssi == -1) null else uriString.substring(0, ssi)
    }

    override val encodedSchemeSpecificPart: String
        get() = getEncodedSchemeSpecificPart()

    override fun getEncodedSchemeSpecificPart(): String {
        val start = ssi + 1
        val end = if (fsi == -1) uriString.length else fsi
        return uriString.substring(start, end)
    }

    override val schemeSpecificPart: String
        get() = getSchemeSpecificPart()

    override fun getSchemeSpecificPart(): String {
        return decode(getEncodedSchemeSpecificPart()) ?: ""
    }

    override val encodedAuthority: String?
        get() = getEncodedAuthority()

    override fun getEncodedAuthority(): String? {
        if (!isHierarchical()) return null
        val length = uriString.length
        if (length > ssi + 2 && uriString[ssi + 1] == '/' && uriString[ssi + 2] == '/') {
            val start = ssi + 3
            var end = start
            while (end < length) {
                when (uriString[end]) {
                    '/', '?', '#' -> return uriString.substring(start, end)
                }
                end++
            }
            return uriString.substring(start, end)
        }
        return null
    }

    override val authority: String?
        get() = getAuthority()

    override fun getAuthority(): String? = decode(getEncodedAuthority())

    override val encodedUserInfo: String?
        get() = getEncodedUserInfo()

    override fun getEncodedUserInfo(): String? {
        val auth = getEncodedAuthority() ?: return null
        val end = auth.lastIndexOf('@')
        return if (end == -1) null else auth.substring(0, end)
    }

    override val userInfo: String?
        get() = getUserInfo()

    override fun getUserInfo(): String? = decode(getEncodedUserInfo())

    private fun findPortSeparator(authority: String?): Int {
        if (authority == null) return -1
        for (i in authority.length - 1 downTo 0) {
            val c = authority[i]
            if (c == ':') return i
            if (c < '0' || c > '9') return -1
        }
        return -1
    }

    override val host: String?
        get() = getHost()

    override fun getHost(): String? {
        val auth = getEncodedAuthority() ?: return null
        val userInfoSeparator = auth.lastIndexOf('@')
        val portSeparator = findPortSeparator(auth)
        val encodedHost = if (portSeparator == -1) {
            auth.substring(userInfoSeparator + 1)
        } else {
            auth.substring(userInfoSeparator + 1, portSeparator)
        }
        return decode(encodedHost)
    }

    override val port: Int
        get() = getPort()

    override fun getPort(): Int {
        val auth = getEncodedAuthority() ?: return -1
        val portSeparator = findPortSeparator(auth)
        if (portSeparator == -1) return -1
        val portString = decode(auth.substring(portSeparator + 1)) ?: return -1
        return try {
            portString.toInt()
        } catch (_: NumberFormatException) {
            -1
        }
    }

    override val encodedPath: String?
        get() = getEncodedPath()

    override fun getEncodedPath(): String? {
        if (!isHierarchical()) return null
        val length = uriString.length
        if (ssi + 1 == length) return ""
        var pathStart = ssi + 1
        if (uriString[pathStart] == '?') return ""
        if (uriString[pathStart] == '#') return ""
        if (length > pathStart + 1 && uriString[pathStart] == '/' && uriString[pathStart + 1] == '/') {
            var next = pathStart + 2
            while (next < length) {
                when (uriString[next]) {
                    '?', '#' -> return ""
                    '/' -> break
                }
                next++
            }
            pathStart = next
        }
        var pathEnd = pathStart
        while (pathEnd < length) {
            when (uriString[pathEnd]) {
                '?', '#' -> break
            }
            pathEnd++
        }
        return uriString.substring(pathStart, pathEnd)
    }

    override val path: String?
        get() = getPath()

    override fun getPath(): String? = decode(getEncodedPath())

    override val pathSegments: List<String>
        get() = getPathSegments()

    override fun getPathSegments(): List<String> {
        val p = getEncodedPath() ?: return emptyList()
        val segments = ArrayList<String>()
        var previous = 0
        var current = p.indexOf('/')
        while (current >= 0) {
            if (previous < current) {
                segments.add(decode(p.substring(previous, current)) ?: "")
            }
            previous = current + 1
            current = p.indexOf('/', previous)
        }
        if (previous < p.length) {
            segments.add(decode(p.substring(previous)) ?: "")
        }
        return Collections.unmodifiableList(segments)
    }

    override val lastPathSegment: String?
        get() = getLastPathSegment()

    override fun getLastPathSegment(): String? {
        val segments = getPathSegments()
        val size = segments.size
        return if (size == 0) null else segments[size - 1]
    }

    private fun findQuerySeparator(): Int {
        val qsi = uriString.indexOf('?', ssi)
        if (qsi == -1) return -1
        return if (fsi != -1 && qsi > fsi) -1 else qsi
    }

    override val encodedQuery: String?
        get() = getEncodedQuery()

    override fun getEncodedQuery(): String? {
        if (!isHierarchical()) return null
        val qsi = findQuerySeparator()
        if (qsi == -1) return null
        val end = if (fsi == -1) uriString.length else fsi
        return uriString.substring(qsi + 1, end)
    }

    override val query: String?
        get() = getQuery()

    override fun getQuery(): String? = decode(getEncodedQuery())

    override val encodedFragment: String?
        get() = getEncodedFragment()

    override fun getEncodedFragment(): String? {
        return if (fsi == -1) null else uriString.substring(fsi + 1)
    }

    override val fragment: String?
        get() = getFragment()

    override fun getFragment(): String? = decode(getEncodedFragment())

    override fun buildUpon(): Builder {
        return if (isHierarchical()) {
            Builder()
                .scheme(getScheme())
                .encodedAuthority(getEncodedAuthority())
                .encodedPath(getEncodedPath())
                .encodedQuery(getEncodedQuery())
                .encodedFragment(getEncodedFragment())
        } else {
            Builder()
                .scheme(getScheme())
                .encodedOpaquePart(getEncodedSchemeSpecificPart())
                .encodedFragment(getEncodedFragment())
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uriString)
    }

    override fun toString(): String = uriString
}
