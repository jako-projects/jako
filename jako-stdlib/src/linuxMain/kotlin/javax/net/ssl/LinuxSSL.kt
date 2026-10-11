@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package javax.net.ssl

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import kotlinx.cinterop.*
import platform.posix.*

internal actual fun createPlatformSSLSocket(s: Socket, host: String, port: Int, autoClose: Boolean): SSLSocket {
    return if (OpenSSLBindings.isAvailable) {
        LinuxOpenSSLSocket(s, host, port, autoClose)
    } else {
        NativeSSLSocket(host, port)
    }
}

private const val SSL_CTRL_SET_TLSEXT_HOSTNAME = 55
private const val TLSEXT_NAMETYPE_host_name = 0

internal object OpenSSLBindings {
    private var sslLib: CPointer<out CPointed>? = null
    private var cryptoLib: CPointer<out CPointed>? = null

    val isAvailable: Boolean

    var TLS_client_method: CPointer<CFunction<() -> CPointer<out CPointed>?>>? = null
    var SSL_CTX_new: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var SSL_CTX_free: CPointer<CFunction<(CPointer<out CPointed>?) -> Unit>>? = null
    var SSL_new: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var SSL_free: CPointer<CFunction<(CPointer<out CPointed>?) -> Unit>>? = null
    var SSL_set_fd: CPointer<CFunction<(CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_ctrl: CPointer<CFunction<(CPointer<out CPointed>?, Int, Long, CPointer<out CPointed>?) -> Long>>? = null
    var SSL_connect: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_read: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_write: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_shutdown: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_get_peer_certificate: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var i2d_X509: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?) -> Int>>? = null
    var X509_free: CPointer<CFunction<(CPointer<out CPointed>?) -> Unit>>? = null

    init {
        var lib = dlopen("libssl.so.3", RTLD_NOW or RTLD_GLOBAL)
        if (lib == null) lib = dlopen("libssl.so.1.1", RTLD_NOW or RTLD_GLOBAL)
        if (lib == null) lib = dlopen("libssl.so", RTLD_NOW or RTLD_GLOBAL)
        sslLib = lib

        var cLib = dlopen("libcrypto.so.3", RTLD_NOW or RTLD_GLOBAL)
        if (cLib == null) cLib = dlopen("libcrypto.so.1.1", RTLD_NOW or RTLD_GLOBAL)
        if (cLib == null) cLib = dlopen("libcrypto.so", RTLD_NOW or RTLD_GLOBAL)
        cryptoLib = cLib

        if (lib != null && cLib != null) {
            TLS_client_method = dlsym(lib, "TLS_client_method")?.reinterpret()
                ?: dlsym(lib, "SSLv23_client_method")?.reinterpret()
            SSL_CTX_new = dlsym(lib, "SSL_CTX_new")?.reinterpret()
            SSL_CTX_free = dlsym(lib, "SSL_CTX_free")?.reinterpret()
            SSL_new = dlsym(lib, "SSL_new")?.reinterpret()
            SSL_free = dlsym(lib, "SSL_free")?.reinterpret()
            SSL_set_fd = dlsym(lib, "SSL_set_fd")?.reinterpret()
            SSL_ctrl = dlsym(lib, "SSL_ctrl")?.reinterpret()
            SSL_connect = dlsym(lib, "SSL_connect")?.reinterpret()
            SSL_read = dlsym(lib, "SSL_read")?.reinterpret()
            SSL_write = dlsym(lib, "SSL_write")?.reinterpret()
            SSL_shutdown = dlsym(lib, "SSL_shutdown")?.reinterpret()

            SSL_get_peer_certificate = dlsym(lib, "SSL_get1_peer_certificate")?.reinterpret()
                ?: dlsym(lib, "SSL_get_peer_certificate")?.reinterpret()

            i2d_X509 = dlsym(cLib, "i2d_X509")?.reinterpret()
            X509_free = dlsym(cLib, "X509_free")?.reinterpret()

            isAvailable = SSL_CTX_new != null && SSL_new != null && SSL_connect != null && SSL_read != null && SSL_write != null
        } else {
            isAvailable = false
        }
    }
}

internal class LinuxOpenSSLSocket(
    private val underlyingSocket: Socket,
    private val host: String,
    private val peerPort: Int,
    private val autoClose: Boolean
) : SSLSocket() {

    private var sslCtxPtr: CPointer<out CPointed>? = null
    private var sslPtr: CPointer<out CPointed>? = null
    private var isHandshakeDone = false
    private var isClosedSocket = false
    private var peerCerts: Array<Certificate> = emptyArray()

    private var clientMode = true
    private var needAuth = false
    private var wantAuth = false
    private var protocols = arrayOf("TLSv1.2", "TLSv1.3")
    private var cipherSuites = arrayOf("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384")

    private val sslInputStream: InputStream by lazy {
        LinuxSSLInputStream(this)
    }

    private val sslOutputStream: OutputStream by lazy {
        LinuxSSLOutputStream(this)
    }

    init {
        if (!underlyingSocket.isConnected()) {
            underlyingSocket.connect(java.net.InetSocketAddress(host, port))
        }
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        if (isClosedSocket) throw SocketException("Socket is closed")

        val method = OpenSSLBindings.TLS_client_method?.invoke() ?: throw SSLException("Failed to get TLS client method")
        val ctx = OpenSSLBindings.SSL_CTX_new?.invoke(method) ?: throw SSLException("Failed to create SSL_CTX")
        sslCtxPtr = ctx

        val ssl = OpenSSLBindings.SSL_new?.invoke(ctx) ?: run {
            OpenSSLBindings.SSL_CTX_free?.invoke(ctx)
            throw SSLException("Failed to create SSL object")
        }
        sslPtr = ssl

        val fd = (underlyingSocket as? java.net.Socket)?.fd ?: -1
        if (fd < 0) throw SSLException("Invalid socket file descriptor")

        OpenSSLBindings.SSL_set_fd?.invoke(ssl, fd)

        memScoped {
            val hostCStr = host.cstr.ptr
            OpenSSLBindings.SSL_ctrl?.invoke(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name.toLong(), hostCStr)
        }

        val ret = OpenSSLBindings.SSL_connect?.invoke(ssl) ?: -1
        if (ret <= 0) {
            close()
            throw SSLException("OpenSSL TLS handshake failed: code $ret")
        }

        val certPtr = OpenSSLBindings.SSL_get_peer_certificate?.invoke(ssl)
        if (certPtr != null) {
            memScoped {
                val outBuf = alloc<CPointerVar<ByteVar>>()
                outBuf.value = null
                val len = OpenSSLBindings.i2d_X509?.invoke(certPtr, outBuf.ptr.reinterpret()) ?: -1
                val bufPtr = outBuf.value
                if (len > 0 && bufPtr != null) {
                    val bytes = ByteArray(len) { i -> bufPtr[i] }
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val cert = certFactory.generateCertificate(ByteArrayInputStream(bytes))
                    peerCerts = arrayOf(cert)
                }
            }
            OpenSSLBindings.X509_free?.invoke(certPtr)
        }

        isHandshakeDone = true
    }

    internal fun performRead(b: ByteArray, off: Int, len: Int): Int {
        if (!isHandshakeDone) startHandshake()
        val ssl = sslPtr ?: throw SocketException("Socket is closed")
        if (len == 0) return 0

        return memScoped {
            val buf = allocArray<ByteVar>(len)
            val readCount = OpenSSLBindings.SSL_read?.invoke(ssl, buf, len) ?: -1
            if (readCount <= 0) {
                -1
            } else {
                for (i in 0 until readCount) {
                    b[off + i] = buf[i]
                }
                readCount
            }
        }
    }

    internal fun performWrite(b: ByteArray, off: Int, len: Int) {
        if (!isHandshakeDone) startHandshake()
        val ssl = sslPtr ?: throw SocketException("Socket is closed")
        if (len == 0) return

        memScoped {
            val buf = allocArray<ByteVar>(len)
            for (i in 0 until len) {
                buf[i] = b[off + i]
            }
            val written = OpenSSLBindings.SSL_write?.invoke(ssl, buf, len) ?: -1
            if (written <= 0) {
                throw SocketException("OpenSSL write failed: $written")
            }
        }
    }

    override fun getInputStream(): InputStream = sslInputStream
    override fun getOutputStream(): OutputStream = sslOutputStream
    override fun getSession(): SSLSession = LinuxSSLSession(host, peerPort, peerCerts)

    override fun setUseClientMode(mode: Boolean) { clientMode = mode }
    override fun getUseClientMode(): Boolean = clientMode
    override fun setNeedClientAuth(need: Boolean) { needAuth = need }
    override fun setWantClientAuth(want: Boolean) { wantAuth = want }
    override fun getEnabledProtocols(): Array<String> = protocols.copyOf()
    override fun setEnabledProtocols(protocols: Array<String>?) { if (protocols != null) this.protocols = protocols.copyOf() }
    override fun getSupportedCipherSuites(): Array<String> = cipherSuites.copyOf()
    override fun getEnabledCipherSuites(): Array<String> = cipherSuites.copyOf()
    override fun setEnabledCipherSuites(suites: Array<String>?) { if (suites != null) this.cipherSuites = suites.copyOf() }

    override fun close() {
        if (!isClosedSocket) {
            isClosedSocket = true
            val ssl = sslPtr
            if (ssl != null) {
                OpenSSLBindings.SSL_shutdown?.invoke(ssl)
                OpenSSLBindings.SSL_free?.invoke(ssl)
                sslPtr = null
            }
            val ctx = sslCtxPtr
            if (ctx != null) {
                OpenSSLBindings.SSL_CTX_free?.invoke(ctx)
                sslCtxPtr = null
            }
            if (autoClose) {
                underlyingSocket.close()
            }
        }
    }
}

private class LinuxSSLInputStream(private val sslSocket: LinuxOpenSSLSocket) : InputStream() {
    override fun read(): Int {
        val b = ByteArray(1)
        val n = sslSocket.performRead(b, 0, 1)
        return if (n <= 0) -1 else b[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return sslSocket.performRead(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}

private class LinuxSSLOutputStream(private val sslSocket: LinuxOpenSSLSocket) : OutputStream() {
    override fun write(b: Int) {
        val bytes = byteArrayOf(b.toByte())
        sslSocket.performWrite(bytes, 0, 1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        sslSocket.performWrite(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}

private class LinuxSSLSession(
    private val host: String,
    private val port: Int,
    private val certs: Array<Certificate>
) : SSLSession {
    override fun getPeerHost(): String? = host
    override fun getPeerPort(): Int = port
    override fun getPeerCertificates(): Array<Certificate> {
        if (certs.isEmpty()) throw SSLPeerUnverifiedException("peer not authenticated")
        return certs.copyOf()
    }
    override fun getCipherSuite(): String = "TLS_AES_128_GCM_SHA256"
    override fun getProtocol(): String = "TLSv1.3"
    override fun isValid(): Boolean = true
}
