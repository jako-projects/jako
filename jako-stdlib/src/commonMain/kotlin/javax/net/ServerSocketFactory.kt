@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.net.ServerSocket
import java.net.InetAddress

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking TCP server sockets require Node.js worker_threads and net module")
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
