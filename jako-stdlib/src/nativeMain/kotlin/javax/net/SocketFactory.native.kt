package javax.net

import java.net.Socket
import java.net.InetAddress

actual abstract class SocketFactory actual protected constructor() {
    actual open fun createSocket(): Socket = Socket()
    actual abstract fun createSocket(host: String, port: Int): Socket
    actual abstract fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket
    actual abstract fun createSocket(host: InetAddress, port: Int): Socket
    actual abstract fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket

    actual companion object {
        actual fun getDefault(): SocketFactory = DefaultSocketFactory()
    }
}

internal class DefaultSocketFactory : SocketFactory() {
    override fun createSocket(host: String, port: Int): Socket = Socket(host, port)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = Socket(host, port)
    override fun createSocket(host: InetAddress, port: Int): Socket = Socket(host, port)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = Socket(address, port)
}
