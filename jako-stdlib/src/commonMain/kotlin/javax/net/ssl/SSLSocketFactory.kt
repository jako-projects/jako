@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.net.Socket
import javax.net.SocketFactory

expect abstract class SSLSocketFactory : SocketFactory {
    constructor()
    abstract fun getDefaultCipherSuites(): Array<String>
    abstract fun getSupportedCipherSuites(): Array<String>
    abstract fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket

    open val defaultCipherSuites: Array<String>
    open val supportedCipherSuites: Array<String>

    companion object {
        fun getDefault(): SSLSocketFactory
    }
}
