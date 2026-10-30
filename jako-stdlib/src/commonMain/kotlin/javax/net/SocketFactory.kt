@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.net.Socket
import java.net.InetAddress

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking TCP sockets require Node.js worker_threads and net module")
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
