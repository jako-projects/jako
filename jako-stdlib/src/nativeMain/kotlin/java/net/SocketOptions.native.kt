package java.net

actual interface SocketOptions {
    actual fun setOption(optID: Int, value: Any?)
    actual fun getOption(optID: Int): Any?

    actual companion object {
        actual val TCP_NODELAY: Int = 0x0001
        actual val SO_BINDADDR: Int = 0x000F
        actual val SO_REUSEADDR: Int = 0x04
        actual val SO_BROADCAST: Int = 0x0020
        actual val IP_MULTICAST_IF: Int = 0x10
        actual val IP_MULTICAST_IF2: Int = 0x1f
        actual val IP_MULTICAST_LOOP: Int = 0x12
        actual val IP_TOS: Int = 0x3
        actual val SO_LINGER: Int = 0x0080
        actual val SO_TIMEOUT: Int = 0x1006
        actual val SO_SNDBUF: Int = 0x1001
        actual val SO_RCVBUF: Int = 0x1002
        actual val SO_KEEPALIVE: Int = 0x0008
        actual val SO_OOBINLINE: Int = 0x1003
    }
}
