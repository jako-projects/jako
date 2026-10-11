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
import platform.posix.INVALID_SOCKET
import platform.windows.*

internal actual fun createPlatformSSLSocket(s: Socket, host: String, port: Int, autoClose: Boolean): SSLSocket {
    return if (WinOpenSSLBindings.isAvailable) {
        MingwOpenSSLSocket(s, host, port, autoClose)
    } else {
        NativeSSLSocket(host, port)
    }
}

private const val SSL_CTRL_SET_TLSEXT_HOSTNAME = 55
private const val TLSEXT_NAMETYPE_host_name = 0

internal object WinOpenSSLBindings {
    private var sslLib: HMODULE? = null
    private var cryptoLib: HMODULE? = null

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
        var lib = LoadLibraryA("libssl-3-x64.dll")
        if (lib == null) lib = LoadLibraryA("libssl-1_1-x64.dll")
        if (lib == null) lib = LoadLibraryA("libssl.dll")
        if (lib == null) lib = LoadLibraryA("ssleay32.dll")
        sslLib = lib

        var cLib = LoadLibraryA("libcrypto-3-x64.dll")
        if (cLib == null) cLib = LoadLibraryA("libcrypto-1_1-x64.dll")
        if (cLib == null) cLib = LoadLibraryA("libcrypto.dll")
        if (cLib == null) cLib = LoadLibraryA("libeay32.dll")
        cryptoLib = cLib

        if (lib != null && cLib != null) {
            TLS_client_method = GetProcAddress(lib, "TLS_client_method")?.reinterpret()
                ?: GetProcAddress(lib, "SSLv23_client_method")?.reinterpret()
            SSL_CTX_new = GetProcAddress(lib, "SSL_CTX_new")?.reinterpret()
            SSL_CTX_free = GetProcAddress(lib, "SSL_CTX_free")?.reinterpret()
            SSL_new = GetProcAddress(lib, "SSL_new")?.reinterpret()
            SSL_free = GetProcAddress(lib, "SSL_free")?.reinterpret()
            SSL_set_fd = GetProcAddress(lib, "SSL_set_fd")?.reinterpret()
            SSL_ctrl = GetProcAddress(lib, "SSL_ctrl")?.reinterpret()
            SSL_connect = GetProcAddress(lib, "SSL_connect")?.reinterpret()
            SSL_read = GetProcAddress(lib, "SSL_read")?.reinterpret()
            SSL_write = GetProcAddress(lib, "SSL_write")?.reinterpret()
            SSL_shutdown = GetProcAddress(lib, "SSL_shutdown")?.reinterpret()

            SSL_get_peer_certificate = GetProcAddress(lib, "SSL_get1_peer_certificate")?.reinterpret()
                ?: GetProcAddress(lib, "SSL_get_peer_certificate")?.reinterpret()

            i2d_X509 = GetProcAddress(cLib, "i2d_X509")?.reinterpret()
            X509_free = GetProcAddress(cLib, "X509_free")?.reinterpret()

            isAvailable = SSL_CTX_new != null && SSL_new != null && SSL_connect != null && SSL_read != null && SSL_write != null
        } else {
            isAvailable = false
        }
    }
}

internal class MingwOpenSSLSocket(
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
        MingwSSLInputStream(this)
    }

    private val sslOutputStream: OutputStream by lazy {
        MingwSSLOutputStream(this)
    }

    init {
        if (!underlyingSocket.isConnected()) {
            underlyingSocket.connect(java.net.InetSocketAddress(host, port))
        }
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        if (isClosedSocket) throw SocketException("Socket is closed")

        val method = WinOpenSSLBindings.TLS_client_method?.invoke() ?: throw SSLException("Failed to get TLS client method")
        val ctx = WinOpenSSLBindings.SSL_CTX_new?.invoke(method) ?: throw SSLException("Failed to create SSL_CTX")
        sslCtxPtr = ctx

        val ssl = WinOpenSSLBindings.SSL_new?.invoke(ctx) ?: run {
            WinOpenSSLBindings.SSL_CTX_free?.invoke(ctx)
            throw SSLException("Failed to create SSL object")
        }
        sslPtr = ssl

        val sockHandle = (underlyingSocket as? java.net.Socket)?.socketHandle ?: INVALID_SOCKET.toULong()
        if (sockHandle == INVALID_SOCKET.toULong()) throw SSLException("Invalid socket handle")

        WinOpenSSLBindings.SSL_set_fd?.invoke(ssl, sockHandle.toInt())

        memScoped {
            val hostCStr = host.cstr.ptr
            WinOpenSSLBindings.SSL_ctrl?.invoke(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name.toLong(), hostCStr)
        }

        val ret = WinOpenSSLBindings.SSL_connect?.invoke(ssl) ?: -1
        if (ret <= 0) {
            close()
            throw SSLException("OpenSSL TLS handshake failed: code $ret")
        }

        val certPtr = WinOpenSSLBindings.SSL_get_peer_certificate?.invoke(ssl)
        if (certPtr != null) {
            memScoped {
                val outBuf = alloc<CPointerVar<ByteVar>>()
                outBuf.value = null
                val len = WinOpenSSLBindings.i2d_X509?.invoke(certPtr, outBuf.ptr.reinterpret()) ?: -1
                val bufPtr = outBuf.value
                if (len > 0 && bufPtr != null) {
                    val bytes = ByteArray(len) { i -> bufPtr[i] }
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val cert = certFactory.generateCertificate(ByteArrayInputStream(bytes))
                    peerCerts = arrayOf(cert)
                }
            }
            WinOpenSSLBindings.X509_free?.invoke(certPtr)
        }

        isHandshakeDone = true
    }

    internal fun performRead(b: ByteArray, off: Int, len: Int): Int {
        if (!isHandshakeDone) startHandshake()
        val ssl = sslPtr ?: throw SocketException("Socket is closed")
        if (len == 0) return 0

        return memScoped {
            val buf = allocArray<ByteVar>(len)
            val readCount = WinOpenSSLBindings.SSL_read?.invoke(ssl, buf, len) ?: -1
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
            val written = WinOpenSSLBindings.SSL_write?.invoke(ssl, buf, len) ?: -1
            if (written <= 0) {
                throw SocketException("OpenSSL write failed: $written")
            }
        }
    }

    override fun getInputStream(): InputStream = sslInputStream
    override fun getOutputStream(): OutputStream = sslOutputStream
    override fun getSession(): SSLSession = MingwSSLSession(host, peerPort, peerCerts)

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
                WinOpenSSLBindings.SSL_shutdown?.invoke(ssl)
                WinOpenSSLBindings.SSL_free?.invoke(ssl)
                sslPtr = null
            }
            val ctx = sslCtxPtr
            if (ctx != null) {
                WinOpenSSLBindings.SSL_CTX_free?.invoke(ctx)
                sslCtxPtr = null
            }
            if (autoClose) {
                underlyingSocket.close()
            }
        }
    }
}

private class MingwSSLInputStream(private val sslSocket: MingwOpenSSLSocket) : InputStream() {
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

private class MingwSSLOutputStream(private val sslSocket: MingwOpenSSLSocket) : OutputStream() {
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

private class MingwSSLSession(
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
