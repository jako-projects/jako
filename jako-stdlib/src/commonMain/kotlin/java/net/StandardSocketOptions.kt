@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect class StandardSocketOptions {
    companion object {
        val SO_BROADCAST: SocketOption<Boolean>
        val SO_KEEPALIVE: SocketOption<Boolean>
        val SO_SNDBUF: SocketOption<Int>
        val SO_RCVBUF: SocketOption<Int>
        val SO_REUSEADDR: SocketOption<Boolean>
        val SO_LINGER: SocketOption<Int>
        val IP_TOS: SocketOption<Int>
        val IP_MULTICAST_IF: SocketOption<NetworkInterface>
        val IP_MULTICAST_TTL: SocketOption<Int>
        val IP_MULTICAST_LOOP: SocketOption<Boolean>
        val TCP_NODELAY: SocketOption<Boolean>
    }
}
