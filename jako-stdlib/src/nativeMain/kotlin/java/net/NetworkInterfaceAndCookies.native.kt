package java.net

import java.lang.Class
import java.lang.Cloneable
import java.lang.System
import java.util.Collections
import java.util.Enumeration

actual interface SocketOption<T> {
    actual fun name(): String
    actual fun type(): Class<T>
}

private class StdSocketOption<T>(
    private val optionName: String,
    private val optionType: Class<T>
) : SocketOption<T> {
    override fun name(): String = optionName
    override fun type(): Class<T> = optionType
    override fun toString(): String = optionName
}

actual class StandardSocketOptions private constructor() {
    actual companion object {
        actual val SO_BROADCAST: SocketOption<Boolean> =
            StdSocketOption("SO_BROADCAST", Class(Boolean::class))
        actual val SO_KEEPALIVE: SocketOption<Boolean> =
            StdSocketOption("SO_KEEPALIVE", Class(Boolean::class))
        actual val SO_SNDBUF: SocketOption<Int> =
            StdSocketOption("SO_SNDBUF", Class(Int::class))
        actual val SO_RCVBUF: SocketOption<Int> =
            StdSocketOption("SO_RCVBUF", Class(Int::class))
        actual val SO_REUSEADDR: SocketOption<Boolean> =
            StdSocketOption("SO_REUSEADDR", Class(Boolean::class))
        actual val SO_LINGER: SocketOption<Int> =
            StdSocketOption("SO_LINGER", Class(Int::class))
        actual val IP_TOS: SocketOption<Int> =
            StdSocketOption("IP_TOS", Class(Int::class))
        actual val IP_MULTICAST_IF: SocketOption<NetworkInterface> =
            StdSocketOption("IP_MULTICAST_IF", Class(NetworkInterface::class))
        actual val IP_MULTICAST_TTL: SocketOption<Int> =
            StdSocketOption("IP_MULTICAST_TTL", Class(Int::class))
        actual val IP_MULTICAST_LOOP: SocketOption<Boolean> =
            StdSocketOption("IP_MULTICAST_LOOP", Class(Boolean::class))
        actual val TCP_NODELAY: SocketOption<Boolean> =
            StdSocketOption("TCP_NODELAY", Class(Boolean::class))
    }
}

actual interface SocketOptions {
    actual fun setOption(optID: Int, value: Any?)
    actual fun getOption(optID: Int): Any?

    actual companion object {
        actual val TCP_NODELAY: Int = 0x0001
        actual val SO_BINDADDR: Int = 0x000F
        actual val SO_REUSEADDR: Int = 0x04
        actual val SO_BROADCAST: Int = 0x0020
        actual val IP_MULTICAST_IF: Int = 0x10
        actual val IP_MULTICAST_IF2: Int = 0x1f
        actual val IP_MULTICAST_LOOP: Int = 0x12
        actual val IP_TOS: Int = 0x3
        actual val SO_LINGER: Int = 0x0080
        actual val SO_TIMEOUT: Int = 0x1006
        actual val SO_SNDBUF: Int = 0x1001
        actual val SO_RCVBUF: Int = 0x1002
        actual val SO_KEEPALIVE: Int = 0x0008
        actual val SO_OOBINLINE: Int = 0x1003
    }
}

actual open class InterfaceAddress internal constructor(
    private val addr: InetAddress,
    private val bcast: InetAddress?,
    private val maskLength: Short
) {
    actual open fun getAddress(): InetAddress = addr
    actual open val address: InetAddress
        get() = addr

    actual open fun getBroadcast(): InetAddress? = bcast
    actual open val broadcast: InetAddress?
        get() = bcast

    actual open fun getNetworkPrefixLength(): Short = maskLength
    actual open val networkPrefixLength: Short
        get() = maskLength

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InterfaceAddress) return false
        return addr == other.addr && bcast == other.bcast && maskLength == other.maskLength
    }

    actual override fun hashCode(): Int {
        return addr.hashCode() + (bcast?.hashCode() ?: 0) + maskLength.toInt()
    }

    actual override fun toString(): String {
        return "$addr/$maskLength [${bcast ?: "null"}]"
    }
}

actual class NetworkInterface internal constructor(
    private val ifName: String,
    private val ifIndex: Int,
    private val addrs: List<InetAddress>,
    private val ifAddrs: List<InterfaceAddress>,
    private val loopbackFlag: Boolean = true,
    private val upFlag: Boolean = true,
    private val p2pFlag: Boolean = false,
    private val multicastFlag: Boolean = true,
    private val hwAddr: ByteArray? = null,
    private val mtuVal: Int = 16384,
    private val virtualFlag: Boolean = false,
    private val parentIf: NetworkInterface? = null
) {
    actual fun getName(): String = ifName
    actual val name: String
        get() = ifName

    actual fun getInetAddresses(): Enumeration<InetAddress> = Collections.enumeration(addrs)
    actual val inetAddresses: Enumeration<InetAddress>
        get() = getInetAddresses()

    actual fun getInterfaceAddresses(): List<InterfaceAddress> = ifAddrs
    actual val interfaceAddresses: List<InterfaceAddress>
        get() = ifAddrs

    actual fun getSubInterfaces(): Enumeration<NetworkInterface> = Collections.enumeration(emptyList())
    actual val subInterfaces: Enumeration<NetworkInterface>
        get() = getSubInterfaces()

    actual fun getParent(): NetworkInterface? = parentIf
    actual val parent: NetworkInterface?
        get() = parentIf

    actual fun getIndex(): Int = ifIndex
    actual val index: Int
        get() = ifIndex

    actual fun getDisplayName(): String? = ifName
    actual val displayName: String?
        get() = ifName

    actual fun isUp(): Boolean = upFlag
    actual val isUp: Boolean
        get() = upFlag

    actual fun isLoopback(): Boolean = loopbackFlag
    actual val isLoopback: Boolean
        get() = loopbackFlag

    actual fun isPointToPoint(): Boolean = p2pFlag
    actual val isPointToPoint: Boolean
        get() = p2pFlag

    actual fun supportsMulticast(): Boolean = multicastFlag

    actual fun getHardwareAddress(): ByteArray? = hwAddr?.copyOf()
    actual val hardwareAddress: ByteArray?
        get() = getHardwareAddress()

    actual fun getMTU(): Int = mtuVal
    actual val mtu: Int
        get() = mtuVal

    actual fun isVirtual(): Boolean = virtualFlag
    actual val isVirtual: Boolean
        get() = virtualFlag

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkInterface) return false
        return ifName == other.ifName && addrs == other.addrs
    }

    actual override fun hashCode(): Int = ifName.hashCode()

    actual override fun toString(): String = "name:$ifName ($ifName)"

    actual companion object {
        private fun defaultInterfaces(): List<NetworkInterface> {
            val osName = System.getProperty("os.name") ?: ""
            val loName = if (osName.contains("Mac", ignoreCase = true) || osName.contains("Darwin", ignoreCase = true)) {
                "lo0"
            } else {
                "lo"
            }
            val loV4 = InetAddress.getByAddress("localhost", byteArrayOf(127, 0, 0, 1))
            val bcastV4 = InetAddress.getByAddress(null, byteArrayOf(127, -1, -1, -1))
            val ifAddrV4 = InterfaceAddress(loV4, bcastV4, 8)
            val loIf = NetworkInterface(
                ifName = loName,
                ifIndex = 1,
                addrs = listOf(loV4),
                ifAddrs = listOf(ifAddrV4),
                loopbackFlag = true,
                upFlag = true
            )
            return listOf(loIf)
        }

        actual fun getByName(name: String): NetworkInterface? {
            for (nif in defaultInterfaces()) {
                if (nif.name == name) return nif
            }
            if (name == "lo" || name == "lo0") {
                val base = defaultInterfaces().first()
                return NetworkInterface(
                    ifName = name,
                    ifIndex = base.index,
                    addrs = base.addrs,
                    ifAddrs = base.ifAddrs,
                    loopbackFlag = true,
                    upFlag = true
                )
            }
            return null
        }

        actual fun getByIndex(index: Int): NetworkInterface? {
            return defaultInterfaces().firstOrNull { it.index == index }
        }

        actual fun getByInetAddress(addr: InetAddress): NetworkInterface? {
            return defaultInterfaces().firstOrNull { nif ->
                nif.addrs.any { it.hostAddress == addr.hostAddress }
            }
        }

        actual fun getNetworkInterfaces(): Enumeration<NetworkInterface>? {
            return Collections.enumeration(defaultInterfaces())
        }
    }
}

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

actual interface CookieStore {
    actual fun add(uri: URI?, cookie: HttpCookie)
    actual fun get(uri: URI): List<HttpCookie>
    actual fun getCookies(): List<HttpCookie>
    actual fun getURIs(): List<URI>
    actual fun remove(uri: URI?, cookie: HttpCookie): Boolean
    actual fun removeAll(): Boolean
}

private class InMemoryCookieStore : CookieStore {
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
