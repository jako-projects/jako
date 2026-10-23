@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.Closeable

/**
 * [Nullability Decision] DatagramSocket remote/local addresses may be null when unbound or disconnected.
 */
expect open class DatagramSocket : Closeable {
    constructor()
    constructor(bindaddr: SocketAddress?)
    constructor(port: Int)
    constructor(port: Int, laddr: InetAddress?)

    open fun bind(addr: SocketAddress?)
    open fun connect(address: InetAddress, port: Int)
    open fun connect(addr: SocketAddress)
    open fun disconnect()
    open fun isBound(): Boolean
    open val isBound: Boolean
    open fun isConnected(): Boolean
    open val isConnected: Boolean
    open fun getInetAddress(): InetAddress?
    open val inetAddress: InetAddress?
    open fun getPort(): Int
    open val port: Int
    open fun getRemoteSocketAddress(): SocketAddress?
    open val remoteSocketAddress: SocketAddress?
    open fun getLocalSocketAddress(): SocketAddress?
    open val localSocketAddress: SocketAddress?
    open fun send(p: DatagramPacket)
    open fun receive(p: DatagramPacket)
    open fun getLocalAddress(): InetAddress
    open val localAddress: InetAddress
    open fun getLocalPort(): Int
    open val localPort: Int
    open fun setSoTimeout(timeout: Int)
    open fun getSoTimeout(): Int
    open var soTimeout: Int
    open fun setSendBufferSize(size: Int)
    open fun getSendBufferSize(): Int
    open var sendBufferSize: Int
    open fun setReceiveBufferSize(size: Int)
    open fun getReceiveBufferSize(): Int
    open var receiveBufferSize: Int
    open fun setReuseAddress(on: Boolean)
    open fun getReuseAddress(): Boolean
    open var reuseAddress: Boolean
    open fun setBroadcast(on: Boolean)
    open fun getBroadcast(): Boolean
    open var broadcast: Boolean
    override fun close()
    open fun isClosed(): Boolean
    open val isClosed: Boolean
}
