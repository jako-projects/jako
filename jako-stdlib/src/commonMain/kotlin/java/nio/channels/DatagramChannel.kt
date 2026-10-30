@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.nio.ByteBuffer

/**
 * [Nullability Decision] receive() returns SocketAddress? (null when in non-blocking mode and no datagram is available).
 */
@WebSupport(WebRuntime.NODE_JS, reason = "DatagramChannel requires Node.js worker_threads and dgram module")
expect abstract class DatagramChannel : SelectableChannel, ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    abstract override fun bind(local: java.net.SocketAddress?): DatagramChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): DatagramChannel
    abstract fun socket(): java.net.DatagramSocket
    abstract fun isConnected(): Boolean
    abstract fun connect(remote: java.net.SocketAddress): DatagramChannel
    abstract fun disconnect(): DatagramChannel
    abstract fun getRemoteAddress(): java.net.SocketAddress?
    abstract override fun getLocalAddress(): java.net.SocketAddress?
    abstract fun receive(dst: ByteBuffer): java.net.SocketAddress?
    abstract fun send(src: ByteBuffer, target: java.net.SocketAddress): Int
    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun read(dsts: Array<ByteBuffer>): Long
    abstract override fun write(src: ByteBuffer): Int
    abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun write(srcs: Array<ByteBuffer>): Long

    companion object {
        fun open(): DatagramChannel
    }
}
