@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.Closeable
import java.io.IOException
import kotlinx.cinterop.*
import platform.posix.*

actual open class DatagramSocket : Closeable {
    internal var sock: ULong = INVALID_SOCKET.convert()
    private var bound: Boolean = false
    private var connected: Boolean = false
    private var closed: Boolean = false
    private var remoteAddr: InetAddress? = null
    private var remotePort: Int = -1
    private var localSockAddr: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0

    actual constructor() : this(InetSocketAddress(0))

    actual constructor(bindaddr: SocketAddress?) {
        WinsockInitializer.ensureInitialized()
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
        if (s == INVALID_SOCKET.convert<ULong>()) {
            throw SocketException("Failed to create datagram socket: WSA error ${WSAGetLastError()}")
        }
        sock = s.convert()
    }

    private fun checkOpen() {
        if (closed || sock == INVALID_SOCKET.convert<ULong>()) throw SocketException("Socket is closed")
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
            sin.sin_port = mingwHtons(port.toUShort())
            sin.sin_addr.S_un.S_addr = ipv4BytesToUInt(bytes)
            if (platform.posix.bind(sock.convert(), sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                throw SocketException("Bind failed: WSA error ${WSAGetLastError()}")
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
            sin.sin_port = mingwHtons(port.toUShort())
            sin.sin_addr.S_un.S_addr = ipv4BytesToUInt(address.getAddress())
            if (platform.posix.connect(sock.convert(), sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                throw SocketException("Connect failed: WSA error ${WSAGetLastError()}")
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
            platform.posix.connect(sock.convert(), sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
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
        if (connected && p.getAddress() != null && (p.getAddress() != remoteAddr || p.getPort() != remotePort)) {
            throw IllegalArgumentException("connected address and packet address differ")
        }
        val data = p.getData()
        val off = p.getOffset()
        val len = p.getLength()
        memScoped {
            val sin = alloc<sockaddr_in>()
            sin.sin_family = AF_INET.convert()
            sin.sin_port = mingwHtons(targetPort.toUShort())
            sin.sin_addr.S_un.S_addr = ipv4BytesToUInt(targetAddr.getAddress())
            val sent = if (len == 0) {
                val dummy = alloc<ByteVar>()
                sendto(sock.convert(), dummy.ptr, 0, 0, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
            } else {
                data.usePinned { pinned ->
                    sendto(sock.convert(), pinned.addressOf(off), len, 0, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                }
            }
            if (sent < 0) {
                throw IOException("sendto failed: WSA error ${WSAGetLastError()}")
            }
        }
    }

    actual open fun receive(p: DatagramPacket) {
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))
        memScoped {
            val timeoutVal = alloc<IntVar>()
            timeoutVal.value = soTimeoutMs
            setsockopt(sock.convert(), SOL_SOCKET, SO_RCVTIMEO, timeoutVal.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())
            val sin = alloc<sockaddr_in>()
            val addrLen = alloc<IntVar>()
            addrLen.value = sizeOf<sockaddr_in>().convert()
            val data = p.getData()
            val off = p.getOffset()
            val cap = p.bufCapacityLength
            val n = if (cap == 0) {
                val dummy = alloc<ByteVar>()
                recvfrom(sock.convert(), dummy.ptr, 0, 0, sin.ptr.reinterpret(), addrLen.ptr)
            } else {
                data.usePinned { pinned ->
                    recvfrom(sock.convert(), pinned.addressOf(off), cap, 0, sin.ptr.reinterpret(), addrLen.ptr)
                }
            }
            if (n < 0) {
                val err = WSAGetLastError()
                if (err == WSAETIMEDOUT) {
                    throw SocketTimeoutException("Receive timed out")
                }
                throw SocketException("recvfrom failed: WSA error $err")
            }
            p.setLength(n)
            val recvPort = mingwNtohs(sin.sin_port).toInt()
            val addrInt = sin.sin_addr.S_un.S_addr
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
        if (sock != INVALID_SOCKET.convert<ULong>()) {
            closesocket(sock.convert())
            sock = INVALID_SOCKET.convert()
        }
    }

    actual open fun isClosed(): Boolean = closed
    actual open val isClosed: Boolean get() = closed

    private fun setSockOptInt(level: Int, optname: Int, value: Int) {
        memScoped {
            val v = alloc<IntVar>()
            v.value = value
            setsockopt(sock.convert(), level, optname, v.ptr.reinterpret<ByteVar>().toKString(), sizeOf<IntVar>().convert())
        }
    }

    private fun getSockOptInt(level: Int, optname: Int): Int {
        return memScoped {
            val v = alloc<IntVar>()
            val len = alloc<IntVar>()
            len.value = sizeOf<IntVar>().convert()
            if (getsockopt(sock.convert(), level, optname, v.ptr.reinterpret<ByteVar>(), len.ptr) == 0) v.value else 0
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
            val len = alloc<IntVar>()
            len.value = sizeOf<sockaddr_in>().convert()
            if (getsockname(sock.convert(), sa.ptr.reinterpret(), len.ptr) == 0) {
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
