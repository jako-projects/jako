package java.net

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.WebTaskScheduler
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.requireWebSupport
import com.bennyhuo.jako.toLatin1ByteArray
import com.bennyhuo.jako.toLatin1String
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

private const val SOCKET_UNSUPPORTED_REASON =
    "Blocking TCP sockets require Node.js worker_threads and net module"

@WebSupport(WebRuntime.NODE_JS, reason = SOCKET_UNSUPPORTED_REASON)
actual open class Socket : Closeable {
    internal var handle: Int = -1
    private var proxy: Proxy? = null
    private var connected: Boolean = false
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var inputShutdown: Boolean = false
    private var outputShutdown: Boolean = false

    private var remoteAddress: SocketAddress? = null
    private var _localSocketAddress: SocketAddress? = null

    private var soTimeoutMs: Int = 0
    private var tcpNoDelayFlag: Boolean = false
    private var soLingerValue: Int = -1
    private var sendBufSize: Int = 65536
    private var recvBufSize: Int = 65536
    private var keepAliveFlag: Boolean = false
    private var reuseAddrFlag: Boolean = false

    private val inputStreamObj: InputStream by lazy {
        object : InputStream() {
            override fun read(): Int {
                val one = ByteArray(1)
                val n = read(one, 0, 1)
                return if (n <= 0) -1 else (one[0].toInt() and 0xFF)
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (closed) throw SocketException("Socket is closed")
                if (inputShutdown) return -1
                if (off < 0 || len < 0 || len > b.size - off) {
                    throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
                }
                if (len == 0) return 0
                if (!connected || handle < 0) throw SocketException("Socket is not connected")

                if (WebTaskScheduler.hasPendingWork()) {
                    WebTaskScheduler.runAllAvailable(16)
                }
                val totalTimeout = if (soTimeoutMs > 0) soTimeoutMs else 30_000
                val deadline = NodeBridge.currentTimeMillis() + totalTimeout
                while (true) {
                    if (closed) throw SocketException("Socket is closed")
                    if (inputShutdown) return -1
                    val rem = (deadline - NodeBridge.currentTimeMillis()).toInt()
                    val sliceMs = if (WebTaskScheduler.hasPendingWork()) {
                        minOf(10, rem.coerceAtLeast(1))
                    } else {
                        rem.coerceAtLeast(1)
                    }
                    val res = NodeBridge.tcpRead(handle, len, sliceMs)
                    if (res == "EOF") {
                        return -1
                    }
                    if (res.startsWith("OK|")) {
                        val payload = res.substring(3).toLatin1ByteArray()
                        payload.copyInto(b, off, 0, payload.size)
                        return payload.size
                    }
                    if (res == "TIMEOUT") {
                        if (WebTaskScheduler.hasPendingWork()) {
                            WebTaskScheduler.runAllAvailable(16)
                        }
                        if (NodeBridge.currentTimeMillis() >= deadline) {
                            throw SocketTimeoutException("Read timed out")
                        }
                        continue
                    }
                    throw SocketException("Socket read failed: $res")
                }
            }

            override fun available(): Int {
                if (closed || inputShutdown || !connected || handle < 0) return 0
                if (WebTaskScheduler.hasPendingWork()) {
                    WebTaskScheduler.runAllAvailable(8)
                }
                return NodeBridge.tcpAvailable(handle)
            }

            override fun close() {
                this@Socket.close()
            }
        }
    }

    private val outputStreamObj: OutputStream by lazy {
        object : OutputStream() {
            override fun write(b: Int) {
                write(byteArrayOf(b.toByte()), 0, 1)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                if (closed) throw SocketException("Socket is closed")
                if (outputShutdown) throw SocketException("Socket output is shutdown")
                if (off < 0 || len < 0 || len > b.size - off) {
                    throw IndexOutOfBoundsException("off=$off, len=$len, size=${b.size}")
                }
                if (len == 0) return
                if (!connected || handle < 0) throw SocketException("Socket is not connected")
                val n = NodeBridge.tcpWrite(handle, b.toLatin1String(off, len))
                if (n < 0) {
                    throw SocketException("Broken pipe or socket write error")
                }
            }

            override fun flush() {}

            override fun close() {
                this@Socket.close()
            }
        }
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

    internal constructor(
        acceptedHandle: Int,
        remoteAddr: SocketAddress,
        localAddr: SocketAddress
    ) {
        this.handle = acceptedHandle
        this.remoteAddress = remoteAddr
        this._localSocketAddress = localAddr
        this.connected = true
        this.bound = true
    }

    actual open fun connect(endpoint: SocketAddress) {
        connect(endpoint, 0)
    }

    actual open fun connect(endpoint: SocketAddress, timeout: Int) {
        requireWebSupport("java.net.Socket.connect(SocketAddress, Int)", WebRuntime.NODE_JS, reason = SOCKET_UNSUPPORTED_REASON)
        if (closed) throw SocketException("Socket is closed")
        if (connected) throw SocketException("Already connected")
        if (timeout < 0) throw IllegalArgumentException("connect: timeout can't be negative")
        if (endpoint !is InetSocketAddress) throw IllegalArgumentException("Unsupported SocketAddress type")
        if (endpoint.isUnresolved()) throw UnknownHostException(endpoint.getHostName())

        val targetAddr = endpoint.getAddress()
        val targetHost = if (targetAddr == null || targetAddr.isAnyLocalAddress()) {
            "127.0.0.1"
        } else {
            targetAddr.getHostAddress()
        }
        val targetPort = endpoint.getPort()
        val localInet = _localSocketAddress as? InetSocketAddress
        val localHost = localInet?.getAddress()?.let { if (it.isAnyLocalAddress()) "" else it.getHostAddress() } ?: ""
        val localPort = localInet?.getPort() ?: 0
        val effectiveTimeout = if (timeout > 0) timeout else 10_000

        val res = NodeBridge.tcpConnect(targetHost, targetPort, effectiveTimeout, localHost, localPort)
        if (res == "TIMEOUT") {
            throw SocketTimeoutException("Connect timed out: $endpoint")
        }
        if (!res.startsWith("OK|")) {
            throw ConnectException("Connection refused: $endpoint ($res)")
        }
        val parts = res.split('|')
        val h = parts[1].toInt()
        val boundLocalAddr = parts[2]
        val boundLocalPort = parts[3].toInt()
        this.handle = h
        this._localSocketAddress = InetSocketAddress(InetAddress.getByName(boundLocalAddr), boundLocalPort)
        this.remoteAddress = endpoint
        this.bound = true
        this.connected = true
        if (tcpNoDelayFlag) {
            NodeBridge.tcpSetOption(h, "TCP_NODELAY", 0, true)
        }
        if (keepAliveFlag) {
            NodeBridge.tcpSetOption(h, "SO_KEEPALIVE", 0, true)
        }
    }

    actual open fun bind(bindpoint: SocketAddress?) {
        requireWebSupport("java.net.Socket.bind(SocketAddress?)", WebRuntime.NODE_JS, reason = SOCKET_UNSUPPORTED_REASON)
        if (closed) throw SocketException("Socket is closed")
        if (bound) throw SocketException("Already bound")
        if (bindpoint != null && bindpoint !is InetSocketAddress) {
            throw IllegalArgumentException("Unsupported address type")
        }
        if (bindpoint != null && bindpoint.isUnresolved()) {
            throw SocketException("SocketAddress is unresolved: $bindpoint")
        }
        val addr = bindpoint?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val port = bindpoint?.getPort() ?: 0
        _localSocketAddress = InetSocketAddress(addr, port)
        bound = true
    }

    actual open fun getInetAddress(): InetAddress? = (remoteAddress as? InetSocketAddress)?.getAddress()

    actual open fun getLocalAddress(): InetAddress =
        (_localSocketAddress as? InetSocketAddress)?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))

    actual open fun getPort(): Int = (remoteAddress as? InetSocketAddress)?.getPort() ?: 0

    actual open fun getLocalPort(): Int = (_localSocketAddress as? InetSocketAddress)?.getPort() ?: -1

    actual open fun getRemoteSocketAddress(): SocketAddress? = remoteAddress

    actual open fun getLocalSocketAddress(): SocketAddress? = _localSocketAddress

    actual open fun getInputStream(): InputStream = inputStreamObj

    actual open fun getOutputStream(): OutputStream = outputStreamObj

    actual override fun close() {
        if (!closed) {
            closed = true
            val h = handle
            handle = -1
            if (h >= 0) {
                NodeBridge.tcpClose(h)
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
        if (closed) throw SocketException("Socket is closed")
        if (!connected) throw SocketException("Socket is not connected")
        inputShutdown = true
        if (handle >= 0) {
            NodeBridge.tcpShutdown(handle, 0)
        }
    }

    actual open fun shutdownOutput() {
        if (closed) throw SocketException("Socket is closed")
        if (!connected) throw SocketException("Socket is not connected")
        outputShutdown = true
        if (handle >= 0) {
            NodeBridge.tcpShutdown(handle, 1)
        }
    }

    actual open fun setSoTimeout(timeout: Int) {
        if (timeout < 0) throw IllegalArgumentException("timeout can't be negative")
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int = soTimeoutMs

    actual open fun setTcpNoDelay(on: Boolean) {
        tcpNoDelayFlag = on
        if (handle >= 0 && !closed) {
            NodeBridge.tcpSetOption(handle, "TCP_NODELAY", 0, on)
        }
    }

    actual open fun getTcpNoDelay(): Boolean = tcpNoDelayFlag

    actual open fun setSoLinger(on: Boolean, linger: Int) {
        soLingerValue = if (on) linger else -1
    }

    actual open fun getSoLinger(): Int = soLingerValue

    actual open fun setSendBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative send size")
        sendBufSize = size
    }

    actual open fun getSendBufferSize(): Int = sendBufSize

    actual open fun setReceiveBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative receive size")
        recvBufSize = size
    }

    actual open fun getReceiveBufferSize(): Int = recvBufSize

    actual open fun setKeepAlive(on: Boolean) {
        keepAliveFlag = on
        if (handle >= 0 && !closed) {
            NodeBridge.tcpSetOption(handle, "SO_KEEPALIVE", 0, on)
        }
    }

    actual open fun getKeepAlive(): Boolean = keepAliveFlag

    actual open fun setReuseAddress(on: Boolean) {
        reuseAddrFlag = on
    }

    actual open fun getReuseAddress(): Boolean = reuseAddrFlag
}
