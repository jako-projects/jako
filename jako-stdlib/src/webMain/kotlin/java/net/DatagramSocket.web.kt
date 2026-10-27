package java.net

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.WebTaskScheduler
import com.bennyhuo.jako.toLatin1ByteArray
import com.bennyhuo.jako.toLatin1String
import java.io.Closeable

actual open class DatagramSocket : Closeable {
    internal var handle: Int = -1
    private var bound: Boolean = false
    private var connected: Boolean = false
    private var closed: Boolean = false
    private var remoteAddr: InetAddress? = null
    private var remotePort: Int = -1
    private var localSockAddr: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0
    private var sendBufSize: Int = 65536
    private var recvBufSize: Int = 65536
    private var reuseAddrFlag: Boolean = false
    private var broadcastFlag: Boolean = false

    actual constructor() : this(InetSocketAddress(0))

    actual constructor(bindaddr: SocketAddress?) {
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

    private fun checkOpen() {
        if (closed) throw SocketException("Socket is closed")
    }

    actual open fun bind(addr: SocketAddress?) {
        checkOpen()
        if (bound) throw SocketException("already bound")
        val target = (addr as? InetSocketAddress)
            ?: if (addr == null) InetSocketAddress(0) else throw IllegalArgumentException("Unsupported address type")
        if (target.isUnresolved()) throw SocketException("Unresolved address")
        val inet = target.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val requestedPort = target.getPort()
        val hostStr = if (inet.isAnyLocalAddress()) "0.0.0.0" else inet.getHostAddress()
        val res = NodeBridge.udpBind(hostStr, requestedPort, reuseAddrFlag)
        if (!res.startsWith("OK|")) {
            throw BindException("Failed to bind DatagramSocket ($hostStr:$requestedPort): $res")
        }
        val parts = res.split('|')
        this.handle = parts[1].toInt()
        val actualPort = parts[2].toInt()
        localSockAddr = InetSocketAddress(inet, actualPort)
        bound = true
        if (broadcastFlag) {
            NodeBridge.udpSetBroadcast(handle, true)
        }
    }

    actual open fun connect(address: InetAddress, port: Int) {
        if (port < 0 || port > 0xFFFF) throw IllegalArgumentException("connect: $port")
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))
        connected = true
        remoteAddr = address
        remotePort = port
    }

    actual open fun connect(addr: SocketAddress) {
        if (addr !is InetSocketAddress) throw IllegalArgumentException("AddressType not supported")
        if (addr.isUnresolved()) throw SocketException("Unresolved address")
        val ia = addr.getAddress() ?: throw IllegalArgumentException("Address is null")
        connect(ia, addr.getPort())
    }

    actual open fun disconnect() {
        if (closed || !connected) return
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
        return localSockAddr
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
        val hostStr = if (targetAddr.isAnyLocalAddress()) "127.0.0.1" else targetAddr.getHostAddress()
        val sent = NodeBridge.udpSend(handle, data.toLatin1String(off, len), hostStr, targetPort)
        if (sent < 0) {
            throw SocketException("UDP send failed")
        }
    }

    actual open fun receive(p: DatagramPacket) {
        checkOpen()
        if (!bound) bind(InetSocketAddress(0))

        if (WebTaskScheduler.hasPendingWork()) {
            WebTaskScheduler.runAllAvailable(16)
        }
        val totalTimeout = if (soTimeoutMs > 0) soTimeoutMs else 30_000
        val deadline = NodeBridge.currentTimeMillis() + totalTimeout
        while (true) {
            checkOpen()
            val rem = (deadline - NodeBridge.currentTimeMillis()).toInt()
            val sliceMs = if (WebTaskScheduler.hasPendingWork()) {
                minOf(10, rem.coerceAtLeast(1))
            } else {
                rem.coerceAtLeast(1)
            }
            val res = NodeBridge.udpReceive(handle, 65536, sliceMs)
            if (res.startsWith("OK|")) {
                val idx1 = res.indexOf('|', 3)
                val idx2 = res.indexOf('|', idx1 + 1)
                val senderHost = res.substring(3, idx1)
                val senderPort = res.substring(idx1 + 1, idx2).toInt()
                val senderInet = InetAddress.getByName(senderHost)
                if (connected && ((remoteAddr != null && senderInet != remoteAddr) || (remotePort >= 0 && senderPort != remotePort))) {
                    continue
                }
                val payload = res.substring(idx2 + 1).toLatin1ByteArray()
                val data = p.getData()
                val off = p.getOffset()
                val cap = p.bufCapacityLength
                val copyLen = minOf(cap, payload.size)
                payload.copyInto(data, off, 0, copyLen)
                p.setLength(copyLen)
                p.setAddress(senderInet)
                p.setPort(senderPort)
                return
            }
            if (res == "TIMEOUT") {
                if (WebTaskScheduler.hasPendingWork()) {
                    WebTaskScheduler.runAllAvailable(16)
                }
                if (NodeBridge.currentTimeMillis() >= deadline) {
                    throw SocketTimeoutException("Receive timed out")
                }
                continue
            }
            throw SocketException("UDP receive failed: $res")
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
        sendBufSize = size
    }

    actual open fun getSendBufferSize(): Int {
        checkOpen()
        return sendBufSize
    }

    actual open var sendBufferSize: Int
        get() = getSendBufferSize()
        set(value) = setSendBufferSize(value)

    actual open fun setReceiveBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative receive size")
        checkOpen()
        recvBufSize = size
    }

    actual open fun getReceiveBufferSize(): Int {
        checkOpen()
        return recvBufSize
    }

    actual open var receiveBufferSize: Int
        get() = getReceiveBufferSize()
        set(value) = setReceiveBufferSize(value)

    actual open fun setReuseAddress(on: Boolean) {
        checkOpen()
        reuseAddrFlag = on
    }

    actual open fun getReuseAddress(): Boolean {
        checkOpen()
        return reuseAddrFlag
    }

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open fun setBroadcast(on: Boolean) {
        checkOpen()
        broadcastFlag = on
        if (bound && handle >= 0) {
            NodeBridge.udpSetBroadcast(handle, on)
        }
    }

    actual open fun getBroadcast(): Boolean {
        checkOpen()
        return broadcastFlag
    }

    actual open var broadcast: Boolean
        get() = getBroadcast()
        set(value) = setBroadcast(value)

    actual override fun close() {
        if (closed) return
        closed = true
        val h = handle
        handle = -1
        if (h >= 0) {
            NodeBridge.udpClose(h)
        }
    }

    actual open fun isClosed(): Boolean = closed
    actual open val isClosed: Boolean get() = closed
}
