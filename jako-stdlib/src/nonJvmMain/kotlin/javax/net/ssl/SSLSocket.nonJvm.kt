package javax.net.ssl

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

actual abstract class SSLSocket actual protected constructor() : Socket() {
    actual abstract fun startHandshake()
    actual abstract fun getSession(): SSLSession
    actual open val session: SSLSession get() = getSession()
    actual abstract fun setUseClientMode(mode: Boolean)
    actual abstract fun getUseClientMode(): Boolean
    actual open fun setNeedClientAuth(need: Boolean) {}
    actual open fun getNeedClientAuth(): Boolean = false
    actual open fun setWantClientAuth(want: Boolean) {}
    actual open fun getWantClientAuth(): Boolean = false
    actual abstract fun setEnabledProtocols(protocols: Array<String>?)
    actual abstract fun getEnabledProtocols(): Array<String>
    actual open var enabledProtocols: Array<String>
        get() = getEnabledProtocols()
        set(value) = setEnabledProtocols(value)

    actual abstract fun getSupportedCipherSuites(): Array<String>
    actual abstract fun setEnabledCipherSuites(suites: Array<String>?)
    actual abstract fun getEnabledCipherSuites(): Array<String>
    actual open var enabledCipherSuites: Array<String>
        get() = getEnabledCipherSuites()
        set(value) = setEnabledCipherSuites(value)

    actual open var useClientMode: Boolean
        get() = getUseClientMode()
        set(value) = setUseClientMode(value)

    actual open var needClientAuth: Boolean
        get() = getNeedClientAuth()
        set(value) = setNeedClientAuth(value)

    actual open var wantClientAuth: Boolean
        get() = getWantClientAuth()
        set(value) = setWantClientAuth(value)

    actual open val supportedCipherSuites: Array<String>
        get() = getSupportedCipherSuites()
}

internal abstract class DelegatingSSLSocket(
    protected val underlyingSocket: Socket,
    protected var peerHostName: String,
    protected var peerPortNum: Int,
    protected val autoClose: Boolean,
    protected val trustManager: X509TrustManager?
) : SSLSocket() {

    protected var clientMode = true
    protected var needAuth = false
    protected var wantAuth = false
    protected var protocols = arrayOf("TLSv1.2", "TLSv1.3")
    protected var cipherSuites = arrayOf(
        "TLS_AES_128_GCM_SHA256",
        "TLS_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_RSA_WITH_AES_128_CBC_SHA"
    )

    protected fun ensureUnderlyingConnected() {
        if (!underlyingSocket.isConnected() && peerHostName.isNotEmpty() && peerPortNum > 0) {
            underlyingSocket.connect(InetSocketAddress(peerHostName, peerPortNum))
        }
    }

    override fun connect(endpoint: SocketAddress) {
        underlyingSocket.connect(endpoint)
        if (endpoint is InetSocketAddress) {
            if (peerHostName.isEmpty()) peerHostName = endpoint.getHostString()
            if (peerPortNum <= 0) peerPortNum = endpoint.getPort()
        }
    }

    override fun connect(endpoint: SocketAddress, timeout: Int) {
        underlyingSocket.connect(endpoint, timeout)
        if (endpoint is InetSocketAddress) {
            if (peerHostName.isEmpty()) peerHostName = endpoint.getHostString()
            if (peerPortNum <= 0) peerPortNum = endpoint.getPort()
        }
    }

    override fun bind(bindpoint: SocketAddress?) = underlyingSocket.bind(bindpoint)
    override fun getInetAddress(): InetAddress? = underlyingSocket.getInetAddress()
    override fun getLocalAddress(): InetAddress = underlyingSocket.getLocalAddress()
    override fun getPort(): Int = if (underlyingSocket.getPort() > 0) underlyingSocket.getPort() else peerPortNum
    override fun getLocalPort(): Int = underlyingSocket.getLocalPort()
    override fun getRemoteSocketAddress(): SocketAddress? = underlyingSocket.getRemoteSocketAddress()
    override fun getLocalSocketAddress(): SocketAddress? = underlyingSocket.getLocalSocketAddress()
    override fun setTcpNoDelay(on: Boolean) = underlyingSocket.setTcpNoDelay(on)
    override fun getTcpNoDelay(): Boolean = underlyingSocket.getTcpNoDelay()
    override fun setSoLinger(on: Boolean, linger: Int) = underlyingSocket.setSoLinger(on, linger)
    override fun getSoLinger(): Int = underlyingSocket.getSoLinger()
    override fun setSoTimeout(timeout: Int) = underlyingSocket.setSoTimeout(timeout)
    override fun getSoTimeout(): Int = underlyingSocket.getSoTimeout()
    override fun setSendBufferSize(size: Int) = underlyingSocket.setSendBufferSize(size)
    override fun getSendBufferSize(): Int = underlyingSocket.getSendBufferSize()
    override fun setReceiveBufferSize(size: Int) = underlyingSocket.setReceiveBufferSize(size)
    override fun getReceiveBufferSize(): Int = underlyingSocket.getReceiveBufferSize()
    override fun setKeepAlive(on: Boolean) = underlyingSocket.setKeepAlive(on)
    override fun getKeepAlive(): Boolean = underlyingSocket.getKeepAlive()
    override fun setReuseAddress(on: Boolean) = underlyingSocket.setReuseAddress(on)
    override fun getReuseAddress(): Boolean = underlyingSocket.getReuseAddress()
    override fun isConnected(): Boolean = underlyingSocket.isConnected()
    override fun isBound(): Boolean = underlyingSocket.isBound()
    override fun isInputShutdown(): Boolean = underlyingSocket.isInputShutdown()
    override fun isOutputShutdown(): Boolean = underlyingSocket.isOutputShutdown()
    override fun shutdownInput() = underlyingSocket.shutdownInput()
    override fun shutdownOutput() = underlyingSocket.shutdownOutput()

    override fun setUseClientMode(mode: Boolean) { clientMode = mode }
    override fun getUseClientMode(): Boolean = clientMode
    override fun setNeedClientAuth(need: Boolean) { needAuth = need }
    override fun getNeedClientAuth(): Boolean = needAuth
    override fun setWantClientAuth(want: Boolean) { wantAuth = want }
    override fun getWantClientAuth(): Boolean = wantAuth

    override fun setEnabledProtocols(protocols: Array<String>?) {
        if (protocols != null) {
            this.protocols = protocols.copyOf()
        }
    }

    override fun getEnabledProtocols(): Array<String> = protocols.copyOf()
    override fun getSupportedCipherSuites(): Array<String> = cipherSuites.copyOf()

    override fun setEnabledCipherSuites(suites: Array<String>?) {
        if (suites != null) {
            this.cipherSuites = suites.copyOf()
        }
    }

    override fun getEnabledCipherSuites(): Array<String> = cipherSuites.copyOf()
}
