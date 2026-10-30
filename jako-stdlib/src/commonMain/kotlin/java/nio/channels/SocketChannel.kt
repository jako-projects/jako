@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.nio.ByteBuffer

/**
 * [Nullability Decision] getRemoteAddress and getLocalAddress return SocketAddress? (null when unconnected/unbound).
 */
@WebSupport(WebRuntime.NODE_JS, reason = "SocketChannel requires Node.js worker_threads and net module")
expect abstract class SocketChannel : SelectableChannel, ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    abstract override fun bind(local: java.net.SocketAddress?): SocketChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): SocketChannel
    abstract fun shutdownInput(): SocketChannel
    abstract fun shutdownOutput(): SocketChannel
    abstract fun socket(): java.net.Socket
    abstract fun isConnected(): Boolean
    abstract fun isConnectionPending(): Boolean
    abstract fun connect(remote: java.net.SocketAddress): Boolean
    abstract fun finishConnect(): Boolean
    abstract fun getRemoteAddress(): java.net.SocketAddress?
    abstract override fun getLocalAddress(): java.net.SocketAddress?
    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun read(dsts: Array<ByteBuffer>): Long
    abstract override fun write(src: ByteBuffer): Int
    abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun write(srcs: Array<ByteBuffer>): Long

    companion object {
        fun open(): SocketChannel
        fun open(remote: java.net.SocketAddress): SocketChannel
    }
}
