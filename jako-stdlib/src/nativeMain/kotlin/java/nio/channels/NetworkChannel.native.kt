package java.nio.channels

import java.net.SocketAddress
import java.net.SocketOption
import kotlin.concurrent.withLock

actual interface NetworkChannel : Channel {
    actual fun bind(local: SocketAddress?): NetworkChannel
    actual fun getLocalAddress(): SocketAddress?
    actual fun <T> setOption(name: SocketOption<T>, value: T?): NetworkChannel
    actual fun <T> getOption(name: SocketOption<T>): T?
    actual fun supportedOptions(): Set<SocketOption<*>>
}
