@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.InputStream
import java.io.OutputStream
import kotlinx.cinterop.*
import platform.posix.*

actual open class Socket : java.io.Closeable {

    private var sock: ULong = INVALID_SOCKET.convert()
    private var proxy: Proxy? = null
    private var connected: Boolean = false
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var inputShutdown: Boolean = false
    private var outputShutdown: Boolean = false

    private var remoteAddress: SocketAddress? = null
    private var _localSocketAddress: SocketAddress? = null

    private var soTimeoutMs: Int = 0
    internal val socketHandle: ULong get() = sock

    private val inputStream: SocketInputStream by lazy {
        SocketInputStream(this) { sock }
    }
    private val outputStream: SocketOutputStream by lazy {
        SocketOutputStream(this) { sock }
    }

    actual constructor() {
        WinsockInitializer.ensureInitialized()
    }

    actual constructor(proxy: Proxy?) : this() {
        this.proxy = proxy
    }

    actual constructor(host: String, port: Int) : this() {
        connect(InetSocketAddress(host, port))
    }

    actual constructor(address: InetAddress, port: Int) : this() {
        connect(InetSocketAddress(address, port))
    }

    internal constructor(sock: ULong, remoteAddr: SocketAddress, localAddr: SocketAddress) {
        WinsockInitializer.ensureInitialized()
        this.sock = sock
        this.remoteAddress = remoteAddr
        this._localSocketAddress = localAddr
        this.connected = true
        this.bound = true
    }

    actual open fun connect(endpoint: SocketAddress) {
        connect(endpoint, 0)
    }

    actual open fun connect(endpoint: SocketAddress, timeout: Int) {
        if (closed) throw SocketException("Socket is closed")
        if (connected) throw SocketException("Already connected")
        if (endpoint !is InetSocketAddress) throw IllegalArgumentException("Unsupported SocketAddress type")

        val inetAddr = endpoint.getAddress() ?: throw UnknownHostException(endpoint.getHostName())
        val port = endpoint.getPort()
        val bytes = inetAddr.getAddress()

        memScoped {
            val socketFd = socket(AF_INET, SOCK_STREAM, 0)
            if (socketFd == INVALID_SOCKET.convert<ULong>()) {
                throw SocketException("Failed to create socket: WSA error ${WSAGetLastError()}")
            }
            this@Socket.sock = socketFd.convert()

            val (saPtr, saLen) = createSockAddr(bytes, port, this)

            val ret = platform.posix.connect(socketFd, saPtr, saLen)
            if (ret < 0) {
                close()
                throw ConnectException("Failed to connect to ${endpoint}: WSA error ${WSAGetLastError()}")
            }

            connected = true
            bound = true
            remoteAddress = endpoint
            _localSocketAddress = updateLocalAddress(socketFd.convert())
        }
    }

    actual open fun bind(bindpoint: SocketAddress?) {
        if (closed) throw SocketException("Socket is closed")
        if (bound) throw SocketException("Already bound")

        val inetOpt = bindpoint as? InetSocketAddress
        val addr = inetOpt?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val port = inetOpt?.getPort() ?: 0
        val bytes = addr.getAddress()

        memScoped {
            if (sock == INVALID_SOCKET.convert<ULong>()) {
                val socketFd = socket(AF_INET, SOCK_STREAM, 0)
                if (socketFd == INVALID_SOCKET.convert<ULong>()) throw SocketException("Failed to create socket: WSA error ${WSAGetLastError()}")
                sock = socketFd.convert()
            }
            val (saPtr, saLen) = createSockAddr(bytes, port, this)
            if (platform.posix.bind(sock.convert(), saPtr, saLen) < 0) {
                throw SocketException("Failed to bind to $bindpoint: WSA error ${WSAGetLastError()}")
            }
            bound = true
            _localSocketAddress = updateLocalAddress(sock.convert())
        }
    }

    actual open fun getInetAddress(): InetAddress? {
        return (remoteAddress as? InetSocketAddress)?.getAddress()
    }

    actual open fun getLocalAddress(): InetAddress {
        return (_localSocketAddress as? InetSocketAddress)?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
    }

    actual open fun getPort(): Int {
        return (remoteAddress as? InetSocketAddress)?.getPort() ?: 0
    }

    actual open fun getLocalPort(): Int {
        return (_localSocketAddress as? InetSocketAddress)?.getPort() ?: 0
    }

    actual open fun getRemoteSocketAddress(): SocketAddress? = remoteAddress

    actual open fun getLocalSocketAddress(): SocketAddress? = _localSocketAddress

    actual open fun getInputStream(): InputStream = inputStream

    actual open fun getOutputStream(): OutputStream = outputStream

    actual override fun close() {
        if (!closed) {
            closed = true
            if (sock != INVALID_SOCKET.convert<ULong>()) {
                closesocket(sock.convert())
                sock = INVALID_SOCKET.convert()
            }
        }
    }

    actual open fun isClosed(): Boolean = closed

    actual open fun isConnected(): Boolean = connected

    actual open fun isBound(): Boolean = bound

    actual open fun isInputShutdown(): Boolean = inputShutdown

    actual open fun isOutputShutdown(): Boolean = outputShutdown

    actual open var soTimeout: Int
        get() = getSoTimeout()
        set(value) = setSoTimeout(value)

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open val isClosed: Boolean get() = isClosed()
    actual open val isConnected: Boolean get() = isConnected()
    actual open val isBound: Boolean get() = isBound()
    actual open val isInputShutdown: Boolean get() = isInputShutdown()
    actual open val isOutputShutdown: Boolean get() = isOutputShutdown()
    actual open val inetAddress: InetAddress? get() = getInetAddress()
    actual open val localAddress: InetAddress get() = getLocalAddress()
    actual open val port: Int get() = getPort()
    actual open val localPort: Int get() = getLocalPort()
    actual open val remoteSocketAddress: SocketAddress? get() = getRemoteSocketAddress()
    actual open val localSocketAddress: SocketAddress? get() = getLocalSocketAddress()

    actual open fun shutdownInput() {
        if (!inputShutdown && sock != INVALID_SOCKET.convert<ULong>()) {
            inputShutdown = true
            shutdown(sock.convert(), SD_RECEIVE)
        }
    }

    actual open fun shutdownOutput() {
        if (!outputShutdown && sock != INVALID_SOCKET.convert<ULong>()) {
            outputShutdown = true
            shutdown(sock.convert(), SD_SEND)
        }
    }

    actual open fun setSoTimeout(timeout: Int) {
        ensureSockCreated()
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            WinsockInitializer.setsockoptInt(sock, SOL_SOCKET, SO_RCVTIMEO, timeout)
        }
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int = soTimeoutMs

    actual open fun setTcpNoDelay(on: Boolean) {
        setSockOptInt(IPPROTO_TCP.convert(), TCP_NODELAY, if (on) 1 else 0)
    }

    actual open fun getTcpNoDelay(): Boolean {
        return getSockOptInt(IPPROTO_TCP.convert(), TCP_NODELAY) != 0
    }

    actual open fun setSoLinger(on: Boolean, linger: Int) {
        ensureSockCreated()
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            memScoped {
                val buf = allocArray<ByteVar>(4)
                buf[0] = (if (on) 1 else 0).toByte()
                buf[1] = 0
                buf[2] = (linger and 0xFF).toByte()
                buf[3] = ((linger shr 8) and 0xFF).toByte()
                WinsockInitializer.setsockoptBytes(sock, SOL_SOCKET, SO_LINGER, buf, 4)
            }
        }
    }

    actual open fun getSoLinger(): Int {
        if (sock == INVALID_SOCKET.convert<ULong>()) return -1
        return memScoped {
            val buf = allocArray<ByteVar>(4)
            val len = alloc<IntVar>()
            len.value = 4
            if (getsockopt(sock.convert(), SOL_SOCKET, SO_LINGER, buf.reinterpret<ByteVar>(), len.ptr) == 0) {
                val on = buf[0].toInt() != 0
                val l = (buf[2].toInt() and 0xFF) or ((buf[3].toInt() and 0xFF) shl 8)
                if (on) l else -1
            } else -1
        }
    }

    actual open fun setSendBufferSize(size: Int) {
        setSockOptInt(SOL_SOCKET, SO_SNDBUF, size)
    }

    actual open fun getSendBufferSize(): Int = getSockOptInt(SOL_SOCKET, SO_SNDBUF)

    actual open fun setReceiveBufferSize(size: Int) {
        setSockOptInt(SOL_SOCKET, SO_RCVBUF, size)
    }

    actual open fun getReceiveBufferSize(): Int = getSockOptInt(SOL_SOCKET, SO_RCVBUF)

    actual open fun setKeepAlive(on: Boolean) {
        setSockOptInt(SOL_SOCKET, SO_KEEPALIVE, if (on) 1 else 0)
    }

    actual open fun getKeepAlive(): Boolean = getSockOptInt(SOL_SOCKET, SO_KEEPALIVE) != 0

    actual open fun setReuseAddress(on: Boolean) {
        setSockOptInt(SOL_SOCKET, SO_REUSEADDR, if (on) 1 else 0)
    }

    actual open fun getReuseAddress(): Boolean = getSockOptInt(SOL_SOCKET, SO_REUSEADDR) != 0

    private fun ensureSockCreated() {
        if (sock == INVALID_SOCKET.convert<ULong>()) {
            val socketFd = socket(AF_INET, SOCK_STREAM, 0)
            if (socketFd != INVALID_SOCKET.convert<ULong>()) {
                sock = socketFd.convert()
            }
        }
    }

    private fun setSockOptInt(level: Int, optname: Int, value: Int) {
        ensureSockCreated()
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            WinsockInitializer.setsockoptInt(sock, level, optname, value)
        }
    }

    private fun getSockOptInt(level: Int, optname: Int): Int {
        ensureSockCreated()
        if (sock == INVALID_SOCKET.convert<ULong>()) return 0
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<IntVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(sock.convert(), level, optname, v.ptr.reinterpret<ByteVar>(), len.ptr) == 0) v.value else 0
        }
    }

    private fun createSockAddr(bytes: ByteArray, port: Int, scope: MemScope): Pair<CPointer<sockaddr>, Int> {
        val sin = scope.alloc<sockaddr_in>()
        sin.sin_family = AF_INET.convert()
        sin.sin_port = mingwHtons(port.toUShort())
        var addrInt = 0u
        if (bytes.size >= 4) {
            addrInt = addrInt or (bytes[0].toUByte().toUInt())
            addrInt = addrInt or (bytes[1].toUByte().toUInt() shl 8)
            addrInt = addrInt or (bytes[2].toUByte().toUInt() shl 16)
            addrInt = addrInt or (bytes[3].toUByte().toUInt() shl 24)
        }
        sin.sin_addr.S_un.S_addr = addrInt
        return Pair(sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
    }

    private fun updateLocalAddress(socketFd: ULong): InetSocketAddress? {
        return memScoped {
            val sa = alloc<sockaddr_in>()
            val len = alloc<IntVar>()
            len.value = sizeOf<sockaddr_in>().convert()
            if (getsockname(socketFd.convert(), sa.ptr.reinterpret(), len.ptr) == 0) {
                val port = mingwNtohs(sa.sin_port).toInt()
                val addrInt = sa.sin_addr.S_un.S_addr
                val b0 = (addrInt and 0xFFu).toByte()
                val b1 = ((addrInt shr 8) and 0xFFu).toByte()
                val b2 = ((addrInt shr 16) and 0xFFu).toByte()
                val b3 = ((addrInt shr 24) and 0xFFu).toByte()
                InetSocketAddress(InetAddress.getByAddress(byteArrayOf(b0, b1, b2, b3)), port)
            } else null
        }
    }
}

internal fun mingwHtons(port: UShort): UShort {
    return ((port.toInt() and 0xFF shl 8) or (port.toInt() shr 8)).toUShort()
}

internal fun mingwNtohs(port: UShort): UShort = mingwHtons(port)
