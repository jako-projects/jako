@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

/**
 * [Nullability Decision] accept() returns SocketChannel? (null when in non-blocking mode and no connection is available).
 */
@WebSupport(WebRuntime.NODE_JS, reason = "ServerSocketChannel requires Node.js worker_threads and net module")
expect abstract class ServerSocketChannel : SelectableChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    override fun bind(local: java.net.SocketAddress?): ServerSocketChannel
    abstract fun bind(local: java.net.SocketAddress?, backlog: Int): ServerSocketChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): ServerSocketChannel
    abstract fun socket(): java.net.ServerSocket
    abstract fun accept(): SocketChannel?
    abstract override fun getLocalAddress(): java.net.SocketAddress?

    companion object {
        fun open(): ServerSocketChannel
    }
}
