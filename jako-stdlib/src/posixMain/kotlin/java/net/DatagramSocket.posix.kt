@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.Closeable
import java.io.IOException
import kotlinx.cinterop.*
import platform.posix.*

actual open class DatagramSocket : Closeable {
    internal var fd: Int = -1
    private var bound: Boolean = false
    private var connected: Boolean = false
    private var closed: Boolean = false
    private var remoteAddr: InetAddress? = null
    private var remotePort: Int = -1
    private var localSockAddr: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0

    actual constructor() : this(InetSocketAddress(0))

    actual constructor(bindaddr: SocketAddress?) {
        createSocket()
        if (bindaddr != null) {
            try {
                bind(bindaddr)
            } catch (t: Throwable) {
                close()
                throw t
            }
        }
    }

    actual constructor(port: Int) : this(port, null)

    actual constructor(port: Int, laddr: InetAddress?) : this(
        InetSocketAddress(laddr ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), port)
    )

    private fun createSocket() {
        val s = socket(AF_INET, SOCK_DGRAM, 0)
        if (s < 0) {
            throw SocketException("Failed to create datagram socket: errno $errno")
        }
        fd = s
    }

    private fun checkOpen() {
        if (closed || fd < 0) throw SocketException("Socket is closed")
    }

    actual open fun bind(addr: SocketAddress?) {
        checkOpen()
        if (bound) throw SocketException("already bound")
        val target = (addr as? InetSocketAddress)
            ?: if (addr == null) InetSocketAddress(0) else throw IllegalArgumentException("Unsupported address type")
        if (target.isUnresolved()) throw SocketException("Unresolved address")
        val inet = target.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val port = target.getPort()
        val bytes = inet.getAddress()

        memScoped {
            val sin = alloc<sockaddr_in>()
            sin.sin_family = AF_INET.convert()
            sin.sin_port = posixHtons(port.toUShort())
            sin.sin_addr.s_addr = ipv4BytesToUInt(bytes)
            if (platform.posix.bind(fd, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                throw SocketException("Bind failed: errno $errno")
            }
            bound = true
            localSockAddr = queryLocalAddress()
        }
    }

    actual open fun connect(address: InetAddress, port: Int) {
        if (port < 0 || port > 0xFFFF) throw IllegalArgumentException("connect: $port")
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))
        memScoped {
            val sin = alloc<sockaddr_in>()
            sin.sin_family = AF_INET.convert()
            sin.sin_port = posixHtons(port.toUShort())
            sin.sin_addr.s_addr = ipv4BytesToUInt(address.getAddress())
            if (platform.posix.connect(fd, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                throw SocketException("Connect failed: errno $errno")
            }
            connected = true
            remoteAddr = address
            remotePort = port
        }
    }

    actual open fun connect(addr: SocketAddress) {
        if (addr !is InetSocketAddress) throw IllegalArgumentException("AddressType not supported")
        if (addr.isUnresolved()) throw SocketException("Unresolved address")
        val ia = addr.getAddress() ?: throw IllegalArgumentException("Address is null")
        connect(ia, addr.getPort())
    }

    actual open fun disconnect() {
        if (closed || !connected) return
        memScoped {
            val sin = alloc<sockaddr_in>()
            sin.sin_family = AF_UNSPEC.convert()
            platform.posix.connect(fd, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
        }
        connected = false
        remoteAddr = null
        remotePort = -1
    }

    actual open fun isBound(): Boolean = bound
    actual open val isBound: Boolean get() = bound

    actual open fun isConnected(): Boolean = connected
    actual open val isConnected: Boolean get() = connected

    actual open fun getInetAddress(): InetAddress? = if (connected) remoteAddr else null
    actual open val inetAddress: InetAddress? get() = getInetAddress()

    actual open fun getPort(): Int = if (connected) remotePort else -1
    actual open val port: Int get() = getPort()

    actual open fun getRemoteSocketAddress(): SocketAddress? {
        if (!connected) return null
        return InetSocketAddress(remoteAddr!!, remotePort)
    }
    actual open val remoteSocketAddress: SocketAddress? get() = getRemoteSocketAddress()

    actual open fun getLocalSocketAddress(): SocketAddress? {
        if (closed || !bound) return null
        return localSockAddr ?: queryLocalAddress()
    }
    actual open val localSocketAddress: SocketAddress? get() = getLocalSocketAddress()

    actual open fun send(p: DatagramPacket) {
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))
        val targetAddr = p.getAddress() ?: remoteAddr ?: throw NullPointerException("Null destination address")
        val targetPort = if (p.getPort() >= 0) p.getPort() else remotePort
        if (targetPort < 0) throw IllegalArgumentException("Port not set")
        if (connected && (targetAddr != remoteAddr || targetPort != remotePort)) {
            if (p.getAddress() != null && (p.getAddress() != remoteAddr || p.getPort() != remotePort)) {
                throw IllegalArgumentException("connected address and packet address differ")
            }
        }
        val data = p.getData()
        val off = p.getOffset()
        val len = p.getLength()
        memScoped {
            val sin = alloc<sockaddr_in>()
            sin.sin_family = AF_INET.convert()
            sin.sin_port = posixHtons(targetPort.toUShort())
            sin.sin_addr.s_addr = ipv4BytesToUInt(targetAddr.getAddress())
            val sent = if (connected) {
                if (len == 0) {
                    val dummy = alloc<ByteVar>()
                    platform.posix.send(fd, dummy.ptr, 0u, 0)
                } else {
                    data.usePinned { pinned ->
                        platform.posix.send(fd, pinned.addressOf(off), len.convert(), 0)
                    }
                }
            } else {
                if (len == 0) {
                    val dummy = alloc<ByteVar>()
                    sendto(fd, dummy.ptr, 0u, 0, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                } else {
                    data.usePinned { pinned ->
                        sendto(fd, pinned.addressOf(off), len.convert(), 0, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                    }
                }
            }
            if (sent < 0) {
                throw IOException("sendto failed: errno $errno")
            }
        }
    }

    actual open fun receive(p: DatagramPacket) {
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))
        memScoped {
            if (soTimeoutMs > 0) {
                val readSet = alloc<fd_set>()
                posix_fd_zero(readSet.ptr)
                posix_fd_set(fd, readSet.ptr)
                val tv = alloc<timeval>()
                tv.tv_sec = (soTimeoutMs / 1000).convert()
                tv.tv_usec = ((soTimeoutMs % 1000) * 1000).convert()
                val sel = select(fd + 1, readSet.ptr, null, null, tv.ptr)
                if (sel == 0) {
                    throw SocketTimeoutException("Receive timed out")
                } else if (sel < 0) {
                    throw SocketException("select failed: errno $errno")
                }
            }
            val sin = alloc<sockaddr_in>()
            val addrLen = alloc<socklen_tVar>()
            addrLen.value = sizeOf<sockaddr_in>().convert()
            val data = p.getData()
            val off = p.getOffset()
            val cap = p.bufCapacityLength
            val n = if (cap == 0) {
                val dummy = alloc<ByteVar>()
                recvfrom(fd, dummy.ptr, 0u, 0, sin.ptr.reinterpret(), addrLen.ptr)
            } else {
                data.usePinned { pinned ->
                    recvfrom(fd, pinned.addressOf(off), cap.convert(), 0, sin.ptr.reinterpret(), addrLen.ptr)
                }
            }
            if (n < 0) {
                val err = errno
                if (err == EAGAIN || err == EWOULDBLOCK) {
                    throw SocketTimeoutException("Receive timed out")
                }
                throw SocketException("recvfrom failed: errno $err")
            }
            p.setLength(n.toInt())
            val recvPort = posixNtohs(sin.sin_port).toInt()
            val addrInt = sin.sin_addr.s_addr
            val b0 = (addrInt and 0xFFu).toByte()
            val b1 = ((addrInt shr 8) and 0xFFu).toByte()
            val b2 = ((addrInt shr 16) and 0xFFu).toByte()
            val b3 = ((addrInt shr 24) and 0xFFu).toByte()
            p.setAddress(InetAddress.getByAddress(byteArrayOf(b0, b1, b2, b3)))
            p.setPort(recvPort)
        }
    }

    actual open fun getLocalAddress(): InetAddress {
        if (closed) return InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        return (getLocalSocketAddress() as? InetSocketAddress)?.getAddress()
            ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
    }
    actual open val localAddress: InetAddress get() = getLocalAddress()

    actual open fun getLocalPort(): Int {
        if (closed) return -1
        if (!bound) return 0
        return (getLocalSocketAddress() as? InetSocketAddress)?.getPort() ?: 0
    }
    actual open val localPort: Int get() = getLocalPort()

    actual open fun setSoTimeout(timeout: Int) {
        if (timeout < 0) throw IllegalArgumentException("timeout can't be negative")
        checkOpen()
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int {
        checkOpen()
        return soTimeoutMs
    }

    actual open var soTimeout: Int
        get() = getSoTimeout()
        set(value) = setSoTimeout(value)

    actual open fun setSendBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative send size")
        checkOpen()
        setSockOptInt(SOL_SOCKET, SO_SNDBUF, size)
    }

    actual open fun getSendBufferSize(): Int {
        checkOpen()
        return getSockOptInt(SOL_SOCKET, SO_SNDBUF)
    }

    actual open var sendBufferSize: Int
        get() = getSendBufferSize()
        set(value) = setSendBufferSize(value)

    actual open fun setReceiveBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative receive size")
        checkOpen()
        setSockOptInt(SOL_SOCKET, SO_RCVBUF, size)
    }

    actual open fun getReceiveBufferSize(): Int {
        checkOpen()
        return getSockOptInt(SOL_SOCKET, SO_RCVBUF)
    }

    actual open var receiveBufferSize: Int
        get() = getReceiveBufferSize()
        set(value) = setReceiveBufferSize(value)

    actual open fun setReuseAddress(on: Boolean) {
        checkOpen()
        setSockOptInt(SOL_SOCKET, SO_REUSEADDR, if (on) 1 else 0)
    }

    actual open fun getReuseAddress(): Boolean {
        checkOpen()
        return getSockOptInt(SOL_SOCKET, SO_REUSEADDR) != 0
    }

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open fun setBroadcast(on: Boolean) {
        checkOpen()
        setSockOptInt(SOL_SOCKET, SO_BROADCAST, if (on) 1 else 0)
    }

    actual open fun getBroadcast(): Boolean {
        checkOpen()
        return getSockOptInt(SOL_SOCKET, SO_BROADCAST) != 0
    }

    actual open var broadcast: Boolean
        get() = getBroadcast()
        set(value) = setBroadcast(value)

    actual override fun close() {
        if (closed) return
        closed = true
        if (fd >= 0) {
            platform.posix.close(fd)
            fd = -1
        }
    }

    actual open fun isClosed(): Boolean = closed
    actual open val isClosed: Boolean get() = closed

    private fun setSockOptInt(level: Int, optname: Int, value: Int) {
        memScoped {
            val v = alloc<IntVar>()
            v.value = value
            setsockopt(fd, level, optname, v.ptr, sizeOf<IntVar>().convert())
        }
    }

    private fun getSockOptInt(level: Int, optname: Int): Int {
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(fd, level, optname, v.ptr, len.ptr) == 0) v.value else 0
        }
    }

    private fun ipv4BytesToUInt(bytes: ByteArray): UInt {
        var addrInt = 0u
        if (bytes.size >= 4) {
            addrInt = addrInt or (bytes[0].toUByte().toUInt())
            addrInt = addrInt or (bytes[1].toUByte().toUInt() shl 8)
            addrInt = addrInt or (bytes[2].toUByte().toUInt() shl 16)
            addrInt = addrInt or (bytes[3].toUByte().toUInt() shl 24)
        }
        return addrInt
    }

    private fun queryLocalAddress(): InetSocketAddress? {
        return memScoped {
            val sa = alloc<sockaddr_in>()
            val len = alloc<socklen_tVar>()
            len.value = sizeOf<sockaddr_in>().convert()
            if (getsockname(fd, sa.ptr.reinterpret(), len.ptr) == 0) {
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
