package java.net

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.WebTaskScheduler
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.requireWebSupport
import java.io.Closeable

private const val SERVER_SOCKET_UNSUPPORTED_REASON =
    "Blocking TCP server sockets require Node.js worker_threads and net module"

@WebSupport(WebRuntime.NODE_JS, reason = SERVER_SOCKET_UNSUPPORTED_REASON)
actual open class ServerSocket : Closeable {
    internal var handle: Int = -1
    private var bound: Boolean = false
    private var closed: Boolean = false
    private var localAddress: InetSocketAddress? = null
    private var soTimeoutMs: Int = 0
    private var reuseAddrFlag: Boolean = true
    private var recvBufSize: Int = 65536

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
        requireWebSupport("java.net.ServerSocket.bind(SocketAddress?, Int)", WebRuntime.NODE_JS, reason = SERVER_SOCKET_UNSUPPORTED_REASON)
        if (closed) throw SocketException("ServerSocket is closed")
        if (bound) throw SocketException("Already bound")
        if (endpoint != null && endpoint !is InetSocketAddress) {
            throw IllegalArgumentException("Unsupported address type")
        }
        if (endpoint != null && endpoint.isUnresolved()) {
            throw SocketException("SocketAddress is unresolved: $endpoint")
        }
        val addr = endpoint?.getAddress() ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        val requestedPort = endpoint?.getPort() ?: 0
        val hostStr = if (addr.isAnyLocalAddress()) "0.0.0.0" else addr.getHostAddress()
        val res = NodeBridge.tcpBindServer(hostStr, requestedPort, backlog, reuseAddrFlag)
        if (!res.startsWith("OK|")) {
            throw BindException("Address already in use or bind failed ($hostStr:$requestedPort): $res")
        }
        val parts = res.split('|')
        this.handle = parts[1].toInt()
        val boundPort = parts[2].toInt()
        localAddress = InetSocketAddress(addr, boundPort)
        bound = true
    }

    internal fun hasPendingConnection(): Boolean {
        if (closed || !bound || handle < 0) return false
        return (NodeBridge.pollHandle(handle, 16) and 16) != 0
    }

    actual open fun getInetAddress(): InetAddress? = localAddress?.getAddress()

    actual open fun getLocalPort(): Int = localAddress?.getPort() ?: -1

    actual open fun getLocalSocketAddress(): SocketAddress? = localAddress

    actual open fun accept(): Socket {
        requireWebSupport("java.net.ServerSocket.accept()", WebRuntime.NODE_JS, reason = SERVER_SOCKET_UNSUPPORTED_REASON)
        if (closed) throw SocketException("ServerSocket is closed")
        if (!bound || handle < 0) throw SocketException("ServerSocket is not bound")

        if (WebTaskScheduler.hasPendingWork()) {
            WebTaskScheduler.runAllAvailable(16)
        }
        val totalTimeout = if (soTimeoutMs > 0) soTimeoutMs else 30_000
        val deadline = NodeBridge.currentTimeMillis() + totalTimeout
        while (true) {
            if (closed) throw SocketException("ServerSocket is closed")
            val rem = (deadline - NodeBridge.currentTimeMillis()).toInt()
            val sliceMs = if (WebTaskScheduler.hasPendingWork()) {
                minOf(10, rem.coerceAtLeast(1))
            } else {
                rem.coerceAtLeast(1)
            }
            val res = NodeBridge.tcpAccept(handle, sliceMs)
            if (res.startsWith("OK|")) {
                val parts = res.split('|')
                val clientHandle = parts[1].toInt()
                val lAddr = parts[2]
                val lPort = parts[3].toInt()
                val rAddr = parts[4]
                val rPort = parts[5].toInt()
                return Socket(
                    acceptedHandle = clientHandle,
                    remoteAddr = InetSocketAddress(InetAddress.getByName(rAddr), rPort),
                    localAddr = InetSocketAddress(InetAddress.getByName(lAddr), lPort)
                )
            }
            if (res == "TIMEOUT") {
                if (WebTaskScheduler.hasPendingWork()) {
                    WebTaskScheduler.runAllAvailable(16)
                }
                if (NodeBridge.currentTimeMillis() >= deadline) {
                    throw SocketTimeoutException("Accept timed out after $soTimeoutMs ms")
                }
                continue
            }
            throw SocketException("Accept failed: $res")
        }
    }

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
    actual open fun isBound(): Boolean = bound

    actual open val inetAddress: InetAddress? get() = getInetAddress()
    actual open val localPort: Int get() = getLocalPort()
    actual open val localSocketAddress: SocketAddress? get() = getLocalSocketAddress()

    actual open fun setSoTimeout(timeout: Int) {
        if (timeout < 0) throw IllegalArgumentException("timeout can't be negative")
        soTimeoutMs = timeout
    }

    actual open fun getSoTimeout(): Int = soTimeoutMs

    actual open var soTimeout: Int
        get() = getSoTimeout()
        set(value) = setSoTimeout(value)

    actual open fun setReuseAddress(on: Boolean) {
        reuseAddrFlag = on
    }

    actual open fun getReuseAddress(): Boolean = reuseAddrFlag

    actual open var reuseAddress: Boolean
        get() = getReuseAddress()
        set(value) = setReuseAddress(value)

    actual open fun setReceiveBufferSize(size: Int) {
        if (size <= 0) throw IllegalArgumentException("negative receive size")
        recvBufSize = size
    }

    actual open fun getReceiveBufferSize(): Int = recvBufSize

    actual open var receiveBufferSize: Int
        get() = getReceiveBufferSize()
        set(value) = setReceiveBufferSize(value)
}

internal actual fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean =
    serverSocket.hasPendingConnection()
