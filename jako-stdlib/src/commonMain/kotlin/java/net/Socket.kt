@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking TCP sockets require Node.js worker_threads and net module")
expect open class Socket : Closeable {
    constructor()
    constructor(host: String, port: Int)
    constructor(address: InetAddress, port: Int)
    constructor(proxy: Proxy?)

    open fun connect(endpoint: SocketAddress)
    open fun connect(endpoint: SocketAddress, timeout: Int)
    open fun bind(bindpoint: SocketAddress?)
    open fun getInetAddress(): InetAddress?
    open fun getLocalAddress(): InetAddress
    open fun getPort(): Int
    open fun getLocalPort(): Int
    open fun getRemoteSocketAddress(): SocketAddress?
    open fun getLocalSocketAddress(): SocketAddress?
    open fun getInputStream(): InputStream
    open fun getOutputStream(): OutputStream
    open fun setTcpNoDelay(on: Boolean)
    open fun getTcpNoDelay(): Boolean
    open fun setSoLinger(on: Boolean, linger: Int)
    open fun getSoLinger(): Int
    open fun setSoTimeout(timeout: Int)
    open fun getSoTimeout(): Int
    open fun setSendBufferSize(size: Int)
    open fun getSendBufferSize(): Int
    open fun setReceiveBufferSize(size: Int)
    open fun getReceiveBufferSize(): Int
    open fun setKeepAlive(on: Boolean)
    open fun getKeepAlive(): Boolean
    open fun setReuseAddress(on: Boolean)
    open fun getReuseAddress(): Boolean
    open fun shutdownInput()
    open fun shutdownOutput()
    override fun close()
    open fun isClosed(): Boolean
    open fun isConnected(): Boolean
    open fun isBound(): Boolean
    open fun isInputShutdown(): Boolean
    open fun isOutputShutdown(): Boolean

    open var soTimeout: Int
    open var reuseAddress: Boolean
    open val isClosed: Boolean
    open val isConnected: Boolean
    open val isBound: Boolean
    open val isInputShutdown: Boolean
    open val isOutputShutdown: Boolean
    open val inetAddress: InetAddress?
    open val localAddress: InetAddress
    open val port: Int
    open val localPort: Int
    open val remoteSocketAddress: SocketAddress?
    open val localSocketAddress: SocketAddress?
}
