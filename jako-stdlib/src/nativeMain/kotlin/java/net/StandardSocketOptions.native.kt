package java.net

import java.lang.Class

internal class StdSocketOption<T>(
    private val optionName: String,
    private val optionType: Class<T>
) : SocketOption<T> {
    override fun name(): String = optionName
    override fun type(): Class<T> = optionType
    override fun toString(): String = optionName
}

actual class StandardSocketOptions private constructor() {
    actual companion object {
        actual val SO_BROADCAST: SocketOption<Boolean> =
            StdSocketOption("SO_BROADCAST", Class(Boolean::class))
        actual val SO_KEEPALIVE: SocketOption<Boolean> =
            StdSocketOption("SO_KEEPALIVE", Class(Boolean::class))
        actual val SO_SNDBUF: SocketOption<Int> =
            StdSocketOption("SO_SNDBUF", Class(Int::class))
        actual val SO_RCVBUF: SocketOption<Int> =
            StdSocketOption("SO_RCVBUF", Class(Int::class))
        actual val SO_REUSEADDR: SocketOption<Boolean> =
            StdSocketOption("SO_REUSEADDR", Class(Boolean::class))
        actual val SO_LINGER: SocketOption<Int> =
            StdSocketOption("SO_LINGER", Class(Int::class))
        actual val IP_TOS: SocketOption<Int> =
            StdSocketOption("IP_TOS", Class(Int::class))
        actual val IP_MULTICAST_IF: SocketOption<NetworkInterface> =
            StdSocketOption("IP_MULTICAST_IF", Class(NetworkInterface::class))
        actual val IP_MULTICAST_TTL: SocketOption<Int> =
            StdSocketOption("IP_MULTICAST_TTL", Class(Int::class))
        actual val IP_MULTICAST_LOOP: SocketOption<Boolean> =
            StdSocketOption("IP_MULTICAST_LOOP", Class(Boolean::class))
        actual val TCP_NODELAY: SocketOption<Boolean> =
            StdSocketOption("TCP_NODELAY", Class(Boolean::class))
    }
}
