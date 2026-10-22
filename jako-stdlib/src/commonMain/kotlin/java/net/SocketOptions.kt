@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect interface SocketOptions {
    fun setOption(optID: Int, value: Any?)
    fun getOption(optID: Int): Any?

    companion object {
        val TCP_NODELAY: Int
        val SO_BINDADDR: Int
        val SO_REUSEADDR: Int
        val SO_BROADCAST: Int
        val IP_MULTICAST_IF: Int
        val IP_MULTICAST_IF2: Int
        val IP_MULTICAST_LOOP: Int
        val IP_TOS: Int
        val SO_LINGER: Int
        val SO_TIMEOUT: Int
        val SO_SNDBUF: Int
        val SO_RCVBUF: Int
        val SO_KEEPALIVE: Int
        val SO_OOBINLINE: Int
    }
}
