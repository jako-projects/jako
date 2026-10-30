@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.Closeable

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking TCP server sockets require Node.js worker_threads and net module")
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
    open fun setSoTimeout(timeout: Int)
    open fun getSoTimeout(): Int
    open var soTimeout: Int
    open fun setReuseAddress(on: Boolean)
    open fun getReuseAddress(): Boolean
    open var reuseAddress: Boolean
    open fun setReceiveBufferSize(size: Int)
    open fun getReceiveBufferSize(): Int
    open var receiveBufferSize: Int
    open fun bind(endpoint: SocketAddress?)
    open fun bind(endpoint: SocketAddress?, backlog: Int)
    open fun accept(): Socket
    override fun close()
    open fun isClosed(): Boolean
    open fun isBound(): Boolean
}
