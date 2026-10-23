@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net

import java.net.ServerSocket
import java.net.InetAddress

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
