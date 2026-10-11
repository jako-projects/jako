@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.io.IOException
import java.io.InterruptedIOException
import java.io.Serializable

expect open class SocketAddress : Serializable {
    constructor()
}

expect open class InetAddress : Serializable {
    open fun getHostName(): String
    open fun getHostAddress(): String
    open fun getAddress(): ByteArray

    open val hostName: String
    open val hostAddress: String
    open val address: ByteArray

    companion object {
        fun getByName(host: String?): InetAddress
        fun getAllByName(host: String?): Array<InetAddress>
        fun getByAddress(addr: ByteArray): InetAddress
        fun getByAddress(host: String?, addr: ByteArray): InetAddress
        fun getLocalHost(): InetAddress
    }
}

expect open class Inet4Address : InetAddress {
    companion object {
        fun getByName(host: String?): Inet4Address
        fun getByAddress(addr: ByteArray): Inet4Address
        fun getByAddress(host: String?, addr: ByteArray): Inet4Address
    }
}

expect open class Inet6Address : InetAddress {
    companion object {
        fun getByName(host: String?): Inet6Address
        fun getByAddress(addr: ByteArray): Inet6Address
        fun getByAddress(host: String?, addr: ByteArray): Inet6Address
    }
}

expect open class InetSocketAddress : SocketAddress {
    constructor(port: Int)
    constructor(hostname: String, port: Int)
    constructor(address: InetAddress, port: Int)
    open fun getPort(): Int
    open fun getAddress(): InetAddress?
    open fun getHostName(): String
    open fun getHostString(): String
    open fun isUnresolved(): Boolean

    open val port: Int
    open val address: InetAddress?
    open val hostName: String
    open val hostString: String
    open val isUnresolved: Boolean

    companion object {
        fun createUnresolved(host: String, port: Int): InetSocketAddress
    }
}

expect open class Socket : Closeable {
    constructor()
    constructor(host: String, port: Int)
    constructor(address: InetAddress, port: Int)
    constructor(proxy: Proxy?)

    open fun connect(endpoint: SocketAddress)
    open fun connect(endpoint: SocketAddress, timeout: Int)
    open fun bind(bindpoint: SocketAddress?)
    open fun getInetAddress(): InetAddress?
    open fun getLocalAddress(): InetAddress
    open fun getPort(): Int
    open fun getLocalPort(): Int
    open fun getRemoteSocketAddress(): SocketAddress?
    open fun getLocalSocketAddress(): SocketAddress?
    open fun getInputStream(): InputStream
    open fun getOutputStream(): OutputStream
    open fun setTcpNoDelay(on: Boolean)
    open fun getTcpNoDelay(): Boolean
    open fun setSoLinger(on: Boolean, linger: Int)
    open fun getSoLinger(): Int
    open fun setSoTimeout(timeout: Int)
    open fun getSoTimeout(): Int
    open fun setSendBufferSize(size: Int)
    open fun getSendBufferSize(): Int
    open fun setReceiveBufferSize(size: Int)
    open fun getReceiveBufferSize(): Int
    open fun setKeepAlive(on: Boolean)
    open fun getKeepAlive(): Boolean
    override fun close()
    open fun isClosed(): Boolean
    open fun isConnected(): Boolean
    open fun isBound(): Boolean
    open fun isInputShutdown(): Boolean
    open fun isOutputShutdown(): Boolean

    open var soTimeout: Int
    open val isClosed: Boolean
    open val isConnected: Boolean
    open val isBound: Boolean
    open val isInputShutdown: Boolean
    open val isOutputShutdown: Boolean
    open val inetAddress: InetAddress?
    open val localAddress: InetAddress
    open val port: Int
    open val localPort: Int
    open val remoteSocketAddress: SocketAddress?
    open val localSocketAddress: SocketAddress?
}

expect open class ServerSocket : Closeable {
    constructor()
    constructor(port: Int)
    constructor(port: Int, backlog: Int)
    constructor(port: Int, backlog: Int, bindAddr: InetAddress?)

    open fun getInetAddress(): InetAddress?
    open fun getLocalPort(): Int
    open fun getLocalSocketAddress(): SocketAddress?

    open val inetAddress: InetAddress?
    open val localPort: Int
    open val localSocketAddress: SocketAddress?
    open fun setReuseAddress(on: Boolean)
    open fun getReuseAddress(): Boolean
    open var reuseAddress: Boolean
    open fun bind(endpoint: SocketAddress?)
    open fun bind(endpoint: SocketAddress?, backlog: Int)
    open fun accept(): Socket
    override fun close()
    open fun isClosed(): Boolean
    open fun isBound(): Boolean
}

expect open class Proxy {
    constructor(type: Type, sa: SocketAddress)
    enum class Type { DIRECT, HTTP, SOCKS }
    fun type(): Type
    fun address(): SocketAddress?

    companion object {
        val NO_PROXY: Proxy
    }
}

expect abstract class ProxySelector {
    constructor()
    abstract fun select(uri: URI?): List<Proxy>
    abstract fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?)

    companion object {
        fun getDefault(): ProxySelector?
        fun setDefault(ps: ProxySelector?)
    }
}

expect open class SocketException : IOException {
    constructor()
    constructor(msg: String?)
}

expect open class UnknownHostException : IOException {
    constructor()
    constructor(message: String?)
}

expect open class ConnectException : SocketException {
    constructor()
    constructor(msg: String?)
}

expect open class SocketTimeoutException : InterruptedIOException {
    constructor()
    constructor(msg: String?)
}

expect open class ProtocolException : IOException {
    constructor()
    constructor(host: String?)
}

expect abstract class URLConnection {
    protected constructor(url: URL?)
}

expect abstract class HttpURLConnection : URLConnection {
    protected constructor(url: URL?)
    abstract fun disconnect()
    abstract fun usingProxy(): Boolean
    abstract fun getResponseCode(): Int
    abstract fun getResponseMessage(): String?

    open val responseCode: Int
    open val responseMessage: String?

    companion object {
        val HTTP_OK: Int
        val HTTP_CREATED: Int
        val HTTP_ACCEPTED: Int
        val HTTP_NOT_AUTHORITATIVE: Int
        val HTTP_NO_CONTENT: Int
        val HTTP_RESET: Int
        val HTTP_PARTIAL: Int
        val HTTP_MULT_CHOICE: Int
        val HTTP_MOVED_PERM: Int
        val HTTP_MOVED_TEMP: Int
        val HTTP_SEE_OTHER: Int
        val HTTP_NOT_MODIFIED: Int
        val HTTP_USE_PROXY: Int
        val HTTP_BAD_REQUEST: Int
        val HTTP_UNAUTHORIZED: Int
        val HTTP_PAYMENT_REQUIRED: Int
        val HTTP_FORBIDDEN: Int
        val HTTP_NOT_FOUND: Int
        val HTTP_BAD_METHOD: Int
        val HTTP_NOT_ACCEPTABLE: Int
        val HTTP_PROXY_AUTH: Int
        val HTTP_CLIENT_TIMEOUT: Int
        val HTTP_CONFLICT: Int
        val HTTP_GONE: Int
        val HTTP_LENGTH_REQUIRED: Int
        val HTTP_PRECON_FAILED: Int
        val HTTP_ENTITY_TOO_LARGE: Int
        val HTTP_REQ_TOO_LONG: Int
        val HTTP_UNSUPPORTED_TYPE: Int
        val HTTP_SERVER_ERROR: Int
        val HTTP_INTERNAL_ERROR: Int
        val HTTP_NOT_IMPLEMENTED: Int
        val HTTP_BAD_GATEWAY: Int
        val HTTP_UNAVAILABLE: Int
        val HTTP_GATEWAY_TIMEOUT: Int
        val HTTP_VERSION: Int
    }
}

expect class PasswordAuthentication(userName: String, password: CharArray) {
    fun getUserName(): String
    fun getPassword(): CharArray
    val userName: String
    val password: CharArray
}

expect abstract class Authenticator() {
    enum class RequestorType {
        PROXY, SERVER
    }

    protected open fun getPasswordAuthentication(): PasswordAuthentication?

    protected open val requestingHost: String?
    protected open val requestingSite: InetAddress
    protected open val requestingPort: Int
    protected open val requestingProtocol: String?
    protected open val requestingPrompt: String?
    protected open val requestingScheme: String?
    protected open val requestingURL: URL?
    protected open val requestorType: RequestorType?

    protected open fun getRequestingHost(): String?
    protected open fun getRequestingSite(): InetAddress
    protected open fun getRequestingPort(): Int
    protected open fun getRequestingProtocol(): String?
    protected open fun getRequestingPrompt(): String?
    protected open fun getRequestingScheme(): String?
    protected open fun getRequestingURL(): URL?
    protected open fun getRequestorType(): RequestorType?

    companion object {
        fun setDefault(a: Authenticator?)
        fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?,
            url: URL?,
            requestorType: RequestorType?
        ): PasswordAuthentication?
        fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?
        ): PasswordAuthentication?
    }
}

expect open class MalformedURLException : IOException {
    constructor()
    constructor(msg: String?)
}

expect open class URISyntaxException : Exception {
    constructor(input: String, reason: String, index: Int)
    constructor(input: String, reason: String)
    open fun getInput(): String
    open fun getReason(): String
    open fun getIndex(): Int
    open val input: String
    open val reason: String
    open val index: Int
}

expect open class UnknownServiceException : IOException {
    constructor()
    constructor(msg: String?)
}

expect class URLEncoder {
    companion object {
        fun encode(s: String, enc: String): String
    }
}

expect class URLDecoder {
    companion object {
        fun decode(s: String, enc: String): String
    }
}

