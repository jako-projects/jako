package javax.net

import java.net.InetAddress
import java.net.ServerSocket

actual abstract class ServerSocketFactory actual protected constructor() {
    actual open fun createServerSocket(): ServerSocket = ServerSocket()

    actual abstract fun createServerSocket(port: Int): ServerSocket

    actual abstract fun createServerSocket(port: Int, backlog: Int): ServerSocket

    actual abstract fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): ServerSocket

    actual companion object {
        actual fun getDefault(): ServerSocketFactory {
            return DefaultNativeServerSocketFactory()
        }
    }
}

internal class DefaultNativeServerSocketFactory : ServerSocketFactory() {
    override fun createServerSocket(port: Int): ServerSocket = ServerSocket(port)

    override fun createServerSocket(port: Int, backlog: Int): ServerSocket = ServerSocket(port, backlog)

    override fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): ServerSocket = ServerSocket(port, backlog, ifAddress)
}
