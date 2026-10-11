@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net

import java.net.Socket
import java.net.ServerSocket
import java.net.InetAddress

expect abstract class SocketFactory {
    protected constructor()
    open fun createSocket(): Socket
    abstract fun createSocket(host: String, port: Int): Socket
    abstract fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket
    abstract fun createSocket(host: InetAddress, port: Int): Socket
    abstract fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket

    companion object {
        fun getDefault(): SocketFactory
    }
}

expect abstract class ServerSocketFactory {
    protected constructor()
    open fun createServerSocket(): ServerSocket
    abstract fun createServerSocket(port: Int): ServerSocket
    abstract fun createServerSocket(port: Int, backlog: Int): ServerSocket
    abstract fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): ServerSocket

    companion object {
        fun getDefault(): ServerSocketFactory
    }
}
