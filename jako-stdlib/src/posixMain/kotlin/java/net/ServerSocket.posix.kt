@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import kotlinx.cinterop.*
import platform.posix.*

actual open class ServerSocket : java.io.Closeable {

    internal var fd: Int = -1
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var localAddress: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0

    actual constructor()

    actual constructor(port: Int) : this(port, 50, null)

    actual constructor(port: Int, backlog: Int) : this(port, backlog, null)

    actual constructor(port: Int, backlog: Int, bindAddr: InetAddress?) {
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
            if (socketFd < 0) throw SocketException("Failed to create server socket: errno $errno")

            val reuse = alloc<IntVar>()
            reuse.value = 1
            setsockopt(socketFd, SOL_SOCKET, SO_REUSEADDR, reuse.ptr, sizeOf<IntVar>().convert())

            this@ServerSocket.fd = socketFd

            val sin = alloc<sockaddr_in>()
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
            if (platform.posix.bind(socketFd, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                close()
                throw SocketException("Failed to bind server socket to $endpoint: errno $errno")
            }

            val actualBacklog = if (backlog <= 0) 50 else backlog
            if (listen(socketFd, actualBacklog) < 0) {
                close()
                throw SocketException("Listen failed: errno $errno")
            }

            bound = true
            localAddress = updateLocalAddress(socketFd)
        }
    }

    actual open fun getInetAddress(): InetAddress? = localAddress?.getAddress()

    actual open fun getLocalPort(): Int = localAddress?.getPort() ?: -1

    actual open fun getLocalSocketAddress(): SocketAddress? = localAddress

    internal fun hasPendingConnection(): Boolean {
        if (closed || !bound || fd < 0) return false
        return memScoped {
            val readFdSet = alloc<fd_set>()
            posix_fd_zero(readFdSet.ptr)
            posix_fd_set(fd, readFdSet.ptr)
            val tv = alloc<timeval>()
            tv.tv_sec = 0.convert()
            tv.tv_usec = 0.convert()
            select(fd + 1, readFdSet.ptr, null, null, tv.ptr) > 0
        }
    }

    actual open fun accept(): Socket {
        if (closed) throw SocketException("ServerSocket is closed")
        if (!bound) throw SocketException("ServerSocket is not bound")

        return memScoped {
            val sa = alloc<sockaddr_in>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<sockaddr_in>().convert()

            if (soTimeoutMs > 0) {
                val readFdSet = alloc<fd_set>()
                posix_fd_zero(readFdSet.ptr)
                posix_fd_set(fd, readFdSet.ptr)

                val tv = alloc<timeval>()
                tv.tv_sec = (soTimeoutMs / 1000).convert()
                tv.tv_usec = ((soTimeoutMs % 1000) * 1000).convert()

                val selRet = select(fd + 1, readFdSet.ptr, null, null, tv.ptr)
                if (selRet == 0) {
                    throw SocketTimeoutException("Accept timed out after $soTimeoutMs ms")
                } else if (selRet < 0) {
                    throw SocketException("Select error during accept: errno $errno")
                }
            }

            val clientFd = platform.posix.accept(fd, sa.ptr.reinterpret(), len.ptr)
            if (clientFd < 0) {
                val err = errno
                if (err == EAGAIN || err == EWOULDBLOCK) {
                    throw SocketTimeoutException("Accept timed out")
                }
                throw SocketException("Accept failed: errno $err")
            }

            val clientPort = posixNtohs(sa.sin_port).toInt()
            val addrInt = sa.sin_addr.s_addr
            val b0 = (addrInt and 0xFFu).toByte()
            val b1 = ((addrInt shr 8) and 0xFFu).toByte()
            val b2 = ((addrInt shr 16) and 0xFFu).toByte()
            val b3 = ((addrInt shr 24) and 0xFFu).toByte()
            val remoteAddr = InetSocketAddress(InetAddress.getByAddress(byteArrayOf(b0, b1, b2, b3)), clientPort)
            val locAddr = localAddress ?: InetSocketAddress(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), getLocalPort())

            Socket(clientFd, remoteAddr, locAddr)
        }
    }

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
        if (fd >= 0) {
            memScoped {
                val v = alloc<IntVar>()
                v.value = if (on) 1 else 0
                setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, v.ptr, sizeOf<IntVar>().convert())
            }
        }
    }

    actual open fun getReuseAddress(): Boolean {
        if (fd < 0) return false
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<socklen_tVar>().convert() // sizeOf<IntVar>
            if (getsockopt(fd, SOL_SOCKET, SO_REUSEADDR, v.ptr, len.ptr) == 0) v.value != 0 else false
        }
    }

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open fun setReceiveBufferSize(size: Int) {
        if (fd >= 0) {
            memScoped {
                val v = alloc<IntVar>()
                v.value = size
                setsockopt(fd, SOL_SOCKET, SO_RCVBUF, v.ptr, sizeOf<IntVar>().convert())
            }
        }
    }

    actual open fun getReceiveBufferSize(): Int {
        if (fd < 0) return 0
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(fd, SOL_SOCKET, SO_RCVBUF, v.ptr, len.ptr) == 0) v.value else 0
        }
    }

    actual open var receiveBufferSize: Int
        get() = getReceiveBufferSize()
        set(value) = setReceiveBufferSize(value)

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

internal actual fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean =
    serverSocket.hasPendingConnection()

