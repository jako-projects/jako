@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * [Nullability Decision] accept() returns SocketChannel? (null when in non-blocking mode and no connection is available).
 */
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
