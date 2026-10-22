package javax.net.ssl

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

actual abstract class SSLSocketFactory actual constructor() : javax.net.SocketFactory() {
    actual abstract fun getDefaultCipherSuites(): Array<String>
    actual abstract fun getSupportedCipherSuites(): Array<String>
    actual abstract fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket

    actual open val defaultCipherSuites: Array<String> get() = getDefaultCipherSuites()
    actual open val supportedCipherSuites: Array<String> get() = getSupportedCipherSuites()

    actual companion object {
        actual fun getDefault(): SSLSocketFactory = NativeSSLSocketFactory(null)
    }
}

internal expect fun createPlatformSSLSocket(
    s: Socket,
    host: String,
    port: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager? = null
): SSLSocket

internal class NativeSSLSocketFactory(
    private val trustManager: X509TrustManager? = null
) : SSLSocketFactory() {
    private val suites = arrayOf(
        "TLS_AES_128_GCM_SHA256",
        "TLS_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_RSA_WITH_AES_128_CBC_SHA"
    )

    override fun getDefaultCipherSuites(): Array<String> = suites.copyOf()
    override fun getSupportedCipherSuites(): Array<String> = suites.copyOf()

    override fun createSocket(): Socket {
        return createPlatformSSLSocket(Socket(), "", 0, true, trustManager)
    }

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return createPlatformSSLSocket(s, host, port, autoClose, trustManager)
    }

    override fun createSocket(host: String, port: Int): Socket {
        val s = Socket(host, port)
        return createPlatformSSLSocket(s, host, port, true, trustManager)
    }

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        val s = Socket()
        s.bind(InetSocketAddress(localHost, localPort))
        s.connect(InetSocketAddress(host, port))
        return createPlatformSSLSocket(s, host, port, true, trustManager)
    }

    override fun createSocket(host: InetAddress, port: Int): Socket {
        val h = host.getHostName().ifEmpty { host.getHostAddress() }
        val s = Socket(host, port)
        return createPlatformSSLSocket(s, h, port, true, trustManager)
    }

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        val h = address.getHostName().ifEmpty { address.getHostAddress() }
        val s = Socket()
        s.bind(InetSocketAddress(localAddress, localPort))
        s.connect(InetSocketAddress(address, port))
        return createPlatformSSLSocket(s, h, port, true, trustManager)
    }
}
