@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import kotlinx.cinterop.*
import platform.posix.*

actual open class ServerSocket : java.io.Closeable {

    private var sock: ULong = INVALID_SOCKET.convert()
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var localAddress: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0

    actual constructor() {
        WinsockInitializer.ensureInitialized()
    }

    actual constructor(port: Int) : this(port, 50, null)

    actual constructor(port: Int, backlog: Int) : this(port, backlog, null)

    actual constructor(port: Int, backlog: Int, bindAddr: InetAddress?) : this() {
        bind(InetSocketAddress(bindAddr ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), port), backlog)
    }

    actual open fun bind(endpoint: SocketAddress?) {
        bind(endpoint, 50)
    }

    actual open fun bind(endpoint: SocketAddress?, backlog: Int) {
        if (closed) throw SocketException("ServerSocket is closed")
        if (bound) throw SocketException("Already bound")

        val inetOpt = endpoint as? InetSocketAddress
        val addr = inetOpt?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val port = inetOpt?.getPort() ?: 0
        val bytes = addr.getAddress()

        memScoped {
            val socketFd = socket(AF_INET, SOCK_STREAM, 0)
            if (socketFd == INVALID_SOCKET.convert<ULong>()) throw SocketException("Failed to create server socket: WSA error ${WSAGetLastError()}")

            val reuse = alloc<IntVar>()
            reuse.value = 1
            setsockopt(socketFd, SOL_SOCKET, SO_REUSEADDR, reuse.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())


            this@ServerSocket.sock = socketFd.convert()

            val sin = alloc<sockaddr_in>()
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
            if (platform.posix.bind(socketFd, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                close()
                throw SocketException("Failed to bind server socket to $endpoint: WSA error ${WSAGetLastError()}")
            }

            val actualBacklog = if (backlog <= 0) 50 else backlog
            if (listen(socketFd, actualBacklog) < 0) {
                close()
                throw SocketException("Listen failed: WSA error ${WSAGetLastError()}")
            }

            bound = true
            localAddress = updateLocalAddress(socketFd.convert())
        }
    }

    actual open fun getInetAddress(): InetAddress? = localAddress?.getAddress()

    actual open fun getLocalPort(): Int = localAddress?.getPort() ?: -1

    actual open fun getLocalSocketAddress(): SocketAddress? = localAddress

    internal fun hasPendingConnection(): Boolean {
        if (closed || !bound || sock == INVALID_SOCKET.convert<ULong>()) return false
        return memScoped {
            val readFdSet = alloc<fd_set>()
            readFdSet.fd_count = 1u
            readFdSet.fd_array[0] = sock.convert()
            val tv = alloc<timeval>()
            tv.tv_sec = 0
            tv.tv_usec = 0
            platform.windows.select(0, readFdSet.ptr.reinterpret(), null, null, tv.ptr.reinterpret()) > 0
        }
    }

    actual open fun accept(): Socket {
        if (closed) throw SocketException("ServerSocket is closed")
        if (!bound) throw SocketException("ServerSocket is not bound")

        return memScoped {
            val timeoutVal = alloc<IntVar>()
            timeoutVal.value = soTimeoutMs
            setsockopt(sock.convert(), SOL_SOCKET, SO_RCVTIMEO, timeoutVal.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())
            val sa = alloc<sockaddr_in>()
            val len = alloc<IntVar>()
            len.value = sizeOf<sockaddr_in>().convert()

            val clientFd = platform.posix.accept(sock.convert(), sa.ptr.reinterpret(), len.ptr)
            if (clientFd == INVALID_SOCKET.convert<ULong>()) {
                val err = WSAGetLastError()
                if (err == WSAEWOULDBLOCK || err == WSAETIMEDOUT) {
                    throw SocketTimeoutException("Accept timed out")
                }
                throw SocketException("Accept failed: WSA error $err")
            }

            val clientPort = mingwNtohs(sa.sin_port).toInt()
            val addrInt = sa.sin_addr.S_un.S_addr
            val b0 = (addrInt and 0xFFu).toByte()
            val b1 = ((addrInt shr 8) and 0xFFu).toByte()
            val b2 = ((addrInt shr 16) and 0xFFu).toByte()
            val b3 = ((addrInt shr 24) and 0xFFu).toByte()
            val remoteAddr = InetSocketAddress(InetAddress.getByAddress(byteArrayOf(b0, b1, b2, b3)), clientPort)
            val locAddr = localAddress ?: InetSocketAddress(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), getLocalPort())

            Socket(clientFd.convert(), remoteAddr, locAddr)
        }
    }

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

    actual open fun isBound(): Boolean = bound

    actual open val inetAddress: InetAddress? get() = getInetAddress()
    actual open val localPort: Int get() = getLocalPort()
    actual open val localSocketAddress: SocketAddress? get() = getLocalSocketAddress()

    actual open fun setSoTimeout(timeout: Int) {
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int = soTimeoutMs

    actual open var soTimeout: Int
        get() = getSoTimeout()
        set(value) = setSoTimeout(value)

    actual open fun setReuseAddress(on: Boolean) {
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            memScoped {
                val v = alloc<IntVar>()
                v.value = if (on) 1 else 0
                setsockopt(sock.convert(), SOL_SOCKET, SO_REUSEADDR, v.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())
            }
        }
    }

    actual open fun getReuseAddress(): Boolean {
        if (sock == INVALID_SOCKET.convert<ULong>()) return false
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<IntVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(sock.convert(), SOL_SOCKET, SO_REUSEADDR, v.ptr.reinterpret<ByteVar>(), len.ptr) == 0) v.value != 0 else false

        }
    }

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open fun setReceiveBufferSize(size: Int) {
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            memScoped {
                val v = alloc<IntVar>()
                v.value = size
                setsockopt(sock.convert(), SOL_SOCKET, SO_RCVBUF, v.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())
            }
        }
    }

    actual open fun getReceiveBufferSize(): Int {
        if (sock == INVALID_SOCKET.convert<ULong>()) return 0
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<IntVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(sock.convert(), SOL_SOCKET, SO_RCVBUF, v.ptr.reinterpret<ByteVar>(), len.ptr) == 0) v.value else 0
        }
    }

    actual open var receiveBufferSize: Int
        get() = getReceiveBufferSize()
        set(value) = setReceiveBufferSize(value)

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

internal actual fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean =
    serverSocket.hasPendingConnection()

