@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * [Nullability Decision] getLocalAddress returns null when the channel is unbound.
 */
expect interface NetworkChannel : Channel {
    fun bind(local: java.net.SocketAddress?): NetworkChannel
    fun getLocalAddress(): java.net.SocketAddress?
    fun <T> setOption(name: java.net.SocketOption<T>, value: T?): NetworkChannel
    fun <T> getOption(name: java.net.SocketOption<T>): T?
    fun supportedOptions(): Set<java.net.SocketOption<*>>
}
