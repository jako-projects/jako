@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.InputStream
import java.io.OutputStream
import kotlinx.cinterop.*
import platform.posix.*

actual open class Socket : java.io.Closeable {

    internal var fd: Int = -1
    private var proxy: Proxy? = null
    private var connected: Boolean = false
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var inputShutdown: Boolean = false
    private var outputShutdown: Boolean = false

    private var remoteAddress: SocketAddress? = null
    private var _localSocketAddress: SocketAddress? = null

    private var soTimeoutMs: Int = 0

    private val inputStream: SocketInputStream by lazy {
        SocketInputStream(this) { fd }
    }
    private val outputStream: SocketOutputStream by lazy {
        SocketOutputStream(this) { fd }
    }

    actual constructor()

    actual constructor(proxy: Proxy?) {
        this.proxy = proxy
    }

    actual constructor(host: String, port: Int) {
        connect(InetSocketAddress(host, port))
    }

    actual constructor(address: InetAddress, port: Int) {
        connect(InetSocketAddress(address, port))
    }

    internal constructor(fd: Int, remoteAddr: SocketAddress, localAddr: SocketAddress) {
        ensureSigPipeIgnored()
        this.fd = fd
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
            ensureSigPipeIgnored()
            val socketFd = socket(AF_INET, SOCK_STREAM, 0)
            if (socketFd < 0) {
                throw SocketException("Failed to create socket: errno $errno")
            }
            this@Socket.fd = socketFd

            val (saPtr, saLen) = createSockAddr(bytes, port, this)

            if (timeout > 0) {
                val flags = fcntl(socketFd, F_GETFL, 0)
                fcntl(socketFd, F_SETFL, flags or O_NONBLOCK)

                val ret = platform.posix.connect(socketFd, saPtr, saLen)
                if (ret < 0) {
                    val err = errno
                    if (err != EINPROGRESS) {
                        close()
                        throw ConnectException("Failed to connect to ${endpoint}: errno $err")
                    }
                    val readFdSet = alloc<fd_set>()
                    val writeFdSet = alloc<fd_set>()
                    posix_fd_zero(readFdSet.ptr)
                    posix_fd_zero(writeFdSet.ptr)
                    posix_fd_set(socketFd, writeFdSet.ptr)

                    val tv = alloc<timeval>()
                    tv.tv_sec = (timeout / 1000).convert()
                    tv.tv_usec = ((timeout % 1000) * 1000).convert()

                    val selRet = select(socketFd + 1, readFdSet.ptr, writeFdSet.ptr, null, tv.ptr)
                    if (selRet == 0) {
                        close()
                        throw SocketTimeoutException("Connect timed out after $timeout ms")
                    } else if (selRet < 0) {
                        close()
                        throw SocketException("Select error during connect: errno $errno")
                    }

                    val optVal = alloc<IntVar>()
                    val optLen = alloc<socklen_tVar>()
                    optLen.value = sizeOf<IntVar>().convert()
                    getsockopt(socketFd, SOL_SOCKET, SO_ERROR, optVal.ptr, optLen.ptr)
                    if (optVal.value != 0) {
                        close()
                        throw ConnectException("Connect failed: errno ${optVal.value}")
                    }
                }
                fcntl(socketFd, F_SETFL, flags)
            } else {
                val ret = platform.posix.connect(socketFd, saPtr, saLen)
                if (ret < 0) {
                    close()
                    throw ConnectException("Failed to connect to ${endpoint}: errno $errno")

                }
            }

            connected = true
            bound = true
            remoteAddress = endpoint
            _localSocketAddress = updateLocalAddress(socketFd)
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
            if (fd < 0) {
                fd = socket(AF_INET, SOCK_STREAM, 0)
                if (fd < 0) throw SocketException("Failed to create socket: errno $errno")
            }
            val (saPtr, saLen) = createSockAddr(bytes, port, this)
            if (platform.posix.bind(fd, saPtr, saLen) < 0) {
                throw SocketException("Failed to bind to $bindpoint: errno $errno")
            }
            bound = true
            _localSocketAddress = updateLocalAddress(fd)
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
            if (fd >= 0) {
                platform.posix.close(fd)
                fd = -1
            }
        }
    }

    actual open fun isClosed(): Boolean = closed

    actual open fun isConnected(): Boolean = connected

    val isOpen: Boolean get() = isConnected() && !isClosed()

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
        if (!inputShutdown && fd >= 0) {
            inputShutdown = true
            shutdown(fd, SHUT_RD)
        }
    }

    actual open fun shutdownOutput() {
        if (!outputShutdown && fd >= 0) {
            outputShutdown = true
            shutdown(fd, SHUT_WR)
        }
    }

    actual open fun setSoTimeout(timeout: Int) {
        if (fd >= 0) {
            memScoped {
                val tv = alloc<timeval>()
                tv.tv_sec = (timeout / 1000).convert()
                tv.tv_usec = ((timeout % 1000) * 1000).convert()
                setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO, tv.ptr, sizeOf<timeval>().convert())
            }
        }
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int = soTimeoutMs

    actual open fun setTcpNoDelay(on: Boolean) {
        setSockOptInt(IPPROTO_TCP, TCP_NODELAY, if (on) 1 else 0)
    }

    actual open fun getTcpNoDelay(): Boolean {
        return getSockOptInt(IPPROTO_TCP, TCP_NODELAY) != 0
    }

    actual open fun setSoLinger(on: Boolean, linger: Int) {
        if (fd >= 0) {
            memScoped {
                val l = alloc<linger>()
                l.l_onoff = (if (on) 1 else 0).convert()
                l.l_linger = linger.convert()
                setsockopt(fd, SOL_SOCKET, SO_LINGER, l.ptr, sizeOf<linger>().convert())
            }
        }
    }

    actual open fun getSoLinger(): Int {
        if (fd < 0) return -1
        return memScoped {
            val l = alloc<linger>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<linger>().convert()
            if (getsockopt(fd, SOL_SOCKET, SO_LINGER, l.ptr, len.ptr) == 0) {
                if (l.l_onoff.toInt() != 0) l.l_linger.toInt() else -1
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

    private fun ensureFdCreated() {
        if (fd < 0) {
            fd = socket(AF_INET, SOCK_STREAM, 0)
        }
    }

    private fun setSockOptInt(level: Int, optname: Int, value: Int) {
        ensureFdCreated()
        if (fd >= 0) {
            memScoped {
                val v = alloc<IntVar>()
                v.value = value
                setsockopt(fd, level, optname, v.ptr, sizeOf<IntVar>().convert())
            }
        }
    }

    private fun getSockOptInt(level: Int, optname: Int): Int {
        ensureFdCreated()
        if (fd < 0) return 0
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(fd, level, optname, v.ptr, len.ptr) == 0) v.value else 0
        }
    }


    private fun createSockAddr(bytes: ByteArray, port: Int, scope: MemScope): Pair<CPointer<sockaddr>, socklen_t> {
        val sin = scope.alloc<sockaddr_in>()
        sin.sin_family = AF_INET.convert()
        sin.sin_port = posixHtons(port.toUShort())
        var addrInt = 0u
        if (bytes.size >= 4) {
            addrInt = addrInt or (bytes[0].toUByte().toUInt())
            addrInt = addrInt or (bytes[1].toUByte().toUInt() shl 8)
            addrInt = addrInt or (bytes[2].toUByte().toUInt() shl 16)
            addrInt = addrInt or (bytes[3].toUByte().toUInt() shl 24)
        }
        sin.sin_addr.s_addr = addrInt
        return Pair(sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
    }

    private fun updateLocalAddress(socketFd: Int): InetSocketAddress? {
        return memScoped {
            val sa = alloc<sockaddr_in>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<sockaddr_in>().convert()
            if (getsockname(socketFd, sa.ptr.reinterpret(), len.ptr) == 0) {
                val port = posixNtohs(sa.sin_port).toInt()
                val addrInt = sa.sin_addr.s_addr
                val b0 = (addrInt and 0xFFu).toByte()
                val b1 = ((addrInt shr 8) and 0xFFu).toByte()
                val b2 = ((addrInt shr 16) and 0xFFu).toByte()
                val b3 = ((addrInt shr 24) and 0xFFu).toByte()
                InetSocketAddress(InetAddress.getByAddress(byteArrayOf(b0, b1, b2, b3)), port)
            } else null
        }
    }
}

internal fun posixHtons(port: UShort): UShort {
    return ((port.toInt() and 0xFF shl 8) or (port.toInt() shr 8)).toUShort()
}

internal fun posixNtohs(port: UShort): UShort = posixHtons(port)

internal fun posix_fd_zero(set: CPointer<fd_set>) {
    memset(set, 0, sizeOf<fd_set>().convert())
}

internal fun posix_fd_set(fd: Int, set: CPointer<fd_set>) {
    val b = set.reinterpret<ByteVar>()
    val byteIndex = fd / 8
    val bitIndex = fd % 8
    b[byteIndex] = (b[byteIndex].toInt() or (1 shl bitIndex)).toByte()
}
