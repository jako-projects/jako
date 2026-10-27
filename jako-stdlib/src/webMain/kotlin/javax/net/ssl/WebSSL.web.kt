package javax.net.ssl

import java.net.Socket

internal actual fun createPlatformSSLSocket(
    s: Socket,
    host: String,
    port: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager?
): SSLSocket = Tls12ClientSocket(s, host, port, autoClose, trustManager)
