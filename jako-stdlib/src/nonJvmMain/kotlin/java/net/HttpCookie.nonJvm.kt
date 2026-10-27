package java.net

import java.lang.Cloneable
import java.lang.System

actual class HttpCookie actual constructor(
    name: String,
    value: String?
) : Cloneable {
    private val cookieName: String
    private var cookieValue: String? = value
    private var commentVal: String? = null
    private var commentURLVal: String? = null
    private var toDiscard: Boolean = false
    private var portlistVal: String? = null
    private var domainVal: String? = null
    private var maxAgeVal: Long = -1L
    private var pathVal: String? = null
    private var secureVal: Boolean = false
    private var versionVal: Int = 1
    private var httpOnlyVal: Boolean = false
    private val whenCreated: Long = System.currentTimeMillis()

    init {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || !isToken(trimmed) || trimmed.startsWith("$")) {
            throw IllegalArgumentException("Illegal cookie name: $name")
        }
        this.cookieName = trimmed
    }

    actual fun hasExpired(): Boolean {
        if (maxAgeVal == 0L) return true
        if (maxAgeVal < 0L) return false
        val deltaSecond = (System.currentTimeMillis() - whenCreated) / 1000
        return deltaSecond > maxAgeVal
    }

    actual fun setComment(purpose: String?) {
        commentVal = purpose
    }

    actual fun getComment(): String? = commentVal

    actual var comment: String?
        get() = commentVal
        set(value) { commentVal = value }

    actual fun setCommentURL(purpose: String?) {
        commentURLVal = purpose
    }

    actual fun getCommentURL(): String? = commentURLVal

    actual var commentURL: String?
        get() = commentURLVal
        set(value) { commentURLVal = value }

    actual fun setDiscard(discard: Boolean) {
        toDiscard = discard
    }

    actual fun getDiscard(): Boolean = toDiscard

    actual var discard: Boolean
        get() = toDiscard
        set(value) { toDiscard = value }

    actual fun setPortlist(ports: String?) {
        portlistVal = ports
    }

    actual fun getPortlist(): String? = portlistVal

    actual var portlist: String?
        get() = portlistVal
        set(value) { portlistVal = value }

    actual fun setDomain(pattern: String?) {
        domainVal = pattern?.lowercase()
    }

    actual fun getDomain(): String? = domainVal

    actual var domain: String?
        get() = domainVal
        set(value) { setDomain(value) }

    actual fun setMaxAge(expiry: Long) {
        maxAgeVal = expiry
    }

    actual fun getMaxAge(): Long = maxAgeVal

    actual var maxAge: Long
        get() = maxAgeVal
        set(value) { maxAgeVal = value }

    actual fun setPath(uri: String?) {
        pathVal = uri
    }

    actual fun getPath(): String? = pathVal

    actual var path: String?
        get() = pathVal
        set(value) { pathVal = value }

    actual fun setSecure(flag: Boolean) {
        secureVal = flag
    }

    actual fun getSecure(): Boolean = secureVal

    actual var secure: Boolean
        get() = secureVal
        set(value) { secureVal = value }

    actual fun getName(): String = cookieName

    actual val name: String
        get() = cookieName

    actual fun setValue(newValue: String?) {
        cookieValue = newValue
    }

    actual fun getValue(): String? = cookieValue

    actual var value: String?
        get() = cookieValue
        set(value) { cookieValue = value }

    actual fun getVersion(): Int = versionVal

    actual fun setVersion(v: Int) {
        if (v != 0 && v != 1) {
            throw IllegalArgumentException("cookie version should be 0 or 1")
        }
        versionVal = v
    }

    actual var version: Int
        get() = versionVal
        set(value) { setVersion(value) }

    actual fun isHttpOnly(): Boolean = httpOnlyVal

    actual fun setHttpOnly(httpOnly: Boolean) {
        httpOnlyVal = httpOnly
    }

    actual var isHttpOnly: Boolean
        get() = httpOnlyVal
        set(value) { httpOnlyVal = value }

    actual fun clone(): Any {
        val copy = HttpCookie(cookieName, cookieValue)
        copy.commentVal = commentVal
        copy.commentURLVal = commentURLVal
        copy.toDiscard = toDiscard
        copy.portlistVal = portlistVal
        copy.domainVal = domainVal
        copy.maxAgeVal = maxAgeVal
        copy.pathVal = pathVal
        copy.secureVal = secureVal
        copy.versionVal = versionVal
        copy.httpOnlyVal = httpOnlyVal
        return copy
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HttpCookie) return false
        if (!cookieName.equals(other.cookieName, ignoreCase = true)) return false
        val d1 = domainVal
        val d2 = other.domainVal
        val domainEq = if (d1 == null) d2 == null else d2 != null && d1.equals(d2, ignoreCase = true)
        if (!domainEq) return false
        val p1 = pathVal
        val p2 = other.pathVal
        return if (p1 == null) p2 == null else p1 == p2
    }

    actual override fun hashCode(): Int {
        val h1 = cookieName.lowercase().hashCode()
        val h2 = domainVal?.lowercase()?.hashCode() ?: 0
        val h3 = pathVal?.hashCode() ?: 0
        return h1 + h2 + h3
    }

    actual override fun toString(): String {
        return if (versionVal > 0 && cookieValue != null && !cookieValue!!.startsWith("\"")) {
            "$cookieName=\"$cookieValue\""
        } else {
            "$cookieName=$cookieValue"
        }
    }

    actual companion object {
        private const val TSPECIALS = ",; ()<>@:\\\"/[]?={}\t"

        private fun isToken(value: String): Boolean {
            for (c in value) {
                if (c.code <= 0x20 || c.code >= 0x7f || TSPECIALS.indexOf(c) != -1) {
                    return false
                }
            }
            return true
        }

        actual fun parse(header: String): List<HttpCookie> {
            var s = header.trim()
            if (s.startsWith("Set-Cookie2:", ignoreCase = true)) {
                s = s.substring("Set-Cookie2:".length).trim()
            } else if (s.startsWith("Set-Cookie:", ignoreCase = true)) {
                s = s.substring("Set-Cookie:".length).trim()
            }
            if (s.isEmpty()) throw IllegalArgumentException("Empty cookie header string")
            val parts = s.split(";")
            val first = parts[0].trim()
            val eqIdx = first.indexOf('=')
            if (eqIdx <= 0) throw IllegalArgumentException("Invalid cookie name-value pair: $first")
            val name = first.substring(0, eqIdx).trim()
            var value = first.substring(eqIdx + 1).trim()
            var detectedVersion = 0
            if (value.length >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length - 1)
            }
            val cookie = HttpCookie(name, value)
            for (i in 1 until parts.size) {
                val attr = parts[i].trim()
                if (attr.isEmpty()) continue
                val aEq = attr.indexOf('=')
                val attrName = (if (aEq >= 0) attr.substring(0, aEq) else attr).trim().lowercase()
                var attrVal = if (aEq >= 0) attr.substring(aEq + 1).trim() else ""
                if (attrVal.length >= 2 && attrVal.startsWith("\"") && attrVal.endsWith("\"")) {
                    attrVal = attrVal.substring(1, attrVal.length - 1)
                }
                when (attrName) {
                    "comment" -> if (cookie.comment == null) cookie.comment = attrVal
                    "commenturl" -> if (cookie.commentURL == null) cookie.commentURL = attrVal
                    "discard" -> cookie.discard = true
                    "domain" -> if (cookie.domain == null) cookie.domain = attrVal
                    "max-age" -> {
                        val age = attrVal.toLongOrNull()
                            ?: throw IllegalArgumentException("Illegal cookie max-age attribute")
                        cookie.maxAge = age
                    }
                    "path" -> if (cookie.path == null) cookie.path = attrVal
                    "port" -> if (cookie.portlist == null) cookie.portlist = attrVal
                    "secure" -> cookie.secure = true
                    "httponly" -> cookie.isHttpOnly = true
                    "version" -> {
                        detectedVersion = attrVal.toIntOrNull() ?: 0
                    }
                }
            }
            cookie.version = detectedVersion
            return listOf(cookie)
        }

        actual fun domainMatches(domain: String, host: String): Boolean {
            if (domain.isEmpty() || host.isEmpty()) return false
            val d = domain.lowercase()
            val h = host.lowercase()
            if (d == ".local") {
                return !h.contains('.')
            }
            val effectiveDomain = if (d.startsWith(".")) d else ".$d"
            val effectiveHost = if (!h.contains('.')) "$h.local" else h
            if (h == d || effectiveHost == d) return true
            if (!effectiveHost.endsWith(effectiveDomain)) return false
            val prefix = effectiveHost.substring(0, effectiveHost.length - effectiveDomain.length)
            return !prefix.contains('.')
        }
    }
}
