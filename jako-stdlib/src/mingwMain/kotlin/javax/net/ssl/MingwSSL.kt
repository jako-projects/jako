@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package javax.net.ssl

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlinx.cinterop.*
import platform.posix.INVALID_SOCKET
import platform.windows.*

internal actual fun createPlatformSSLSocket(
    s: Socket,
    host: String,
    port: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager?
): SSLSocket {
    return if (WinOpenSSLBindings.isAvailable) {
        MingwOpenSSLSocket(s, host, port, autoClose, trustManager)
    } else {
        Tls12ClientSocket(s, host, port, autoClose, trustManager)
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
    var SSL_CTX_set_default_verify_paths: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_CTX_set_cipher_list: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<ByteVar>?) -> Int>>? = null
    var SSL_new: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var SSL_free: CPointer<CFunction<(CPointer<out CPointed>?) -> Unit>>? = null
    var SSL_set_fd: CPointer<CFunction<(CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_ctrl: CPointer<CFunction<(CPointer<out CPointed>?, Int, Int, CPointer<out CPointed>?) -> Int>>? = null
    var SSL_connect: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_read: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_write: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?, Int) -> Int>>? = null
    var SSL_shutdown: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_get_peer_certificate: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var SSL_get_verify_result: CPointer<CFunction<(CPointer<out CPointed>?) -> Int>>? = null
    var SSL_get_version: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<ByteVar>?>>? = null
    var SSL_get_current_cipher: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<out CPointed>?>>? = null
    var SSL_CIPHER_get_name: CPointer<CFunction<(CPointer<out CPointed>?) -> CPointer<ByteVar>?>>? = null
    var i2d_X509: CPointer<CFunction<(CPointer<out CPointed>?, CPointer<out CPointed>?) -> Int>>? = null
    var X509_free: CPointer<CFunction<(CPointer<out CPointed>?) -> Unit>>? = null

    init {
        val candidatePairs = listOf(
            "libcrypto-3-x64.dll" to "libssl-3-x64.dll",
            "libcrypto-1_1-x64.dll" to "libssl-1_1-x64.dll",
            "libcrypto.dll" to "libssl.dll",
            "libeay32.dll" to "ssleay32.dll"
        )

        var cLib: HMODULE? = null
        var lib: HMODULE? = null
        for ((cryptoName, sslName) in candidatePairs) {
            val loadedCrypto = LoadLibraryA(cryptoName)
            if (loadedCrypto != null) {
                val loadedSsl = LoadLibraryA(sslName)
                if (loadedSsl != null) {
                    cLib = loadedCrypto
                    lib = loadedSsl
                    break
                } else {
                    FreeLibrary(loadedCrypto)
                }
            }
        }
        cryptoLib = cLib
        sslLib = lib

        if (lib != null && cLib != null) {
            TLS_client_method = GetProcAddress(lib, "TLS_client_method")?.reinterpret()
                ?: GetProcAddress(lib, "SSLv23_client_method")?.reinterpret()
            SSL_CTX_new = GetProcAddress(lib, "SSL_CTX_new")?.reinterpret()
            SSL_CTX_free = GetProcAddress(lib, "SSL_CTX_free")?.reinterpret()
            SSL_CTX_set_default_verify_paths = GetProcAddress(lib, "SSL_CTX_set_default_verify_paths")?.reinterpret()
            SSL_CTX_set_cipher_list = GetProcAddress(lib, "SSL_CTX_set_cipher_list")?.reinterpret()
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
            SSL_get_verify_result = GetProcAddress(lib, "SSL_get_verify_result")?.reinterpret()
            SSL_get_version = GetProcAddress(lib, "SSL_get_version")?.reinterpret()
            SSL_get_current_cipher = GetProcAddress(lib, "SSL_get_current_cipher")?.reinterpret()
            SSL_CIPHER_get_name = GetProcAddress(lib, "SSL_CIPHER_get_name")?.reinterpret()

            i2d_X509 = GetProcAddress(cLib, "i2d_X509")?.reinterpret()
            X509_free = GetProcAddress(cLib, "X509_free")?.reinterpret()

            isAvailable = SSL_CTX_new != null && SSL_new != null && SSL_connect != null && SSL_read != null && SSL_write != null
        } else {
            isAvailable = false
        }
    }
}

internal class MingwOpenSSLSocket(
    underlyingSocket: Socket,
    host: String,
    peerPort: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager? = null
) : DelegatingSSLSocket(underlyingSocket, host, peerPort, autoClose, trustManager) {

    private var sslCtxPtr: CPointer<out CPointed>? = null
    private var sslPtr: CPointer<out CPointed>? = null
    private var isHandshakeDone = false
    private var isClosedSocket = false
    private var peerCerts: Array<Certificate> = emptyArray()
    private var negotiatedProtocol: String = "TLSv1.2"
    private var negotiatedCipherSuite: String = "TLS_AES_128_GCM_SHA256"

    private val sslInputStream: InputStream by lazy {
        MingwSSLInputStream(this)
    }

    private val sslOutputStream: OutputStream by lazy {
        MingwSSLOutputStream(this)
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        if (isClosedSocket) throw SocketException("Socket is closed")
        ensureUnderlyingConnected()

        val method = WinOpenSSLBindings.TLS_client_method?.invoke() ?: throw SSLException("Failed to get TLS client method")
        val ctx = WinOpenSSLBindings.SSL_CTX_new?.invoke(method) ?: throw SSLException("Failed to create SSL_CTX")
        sslCtxPtr = ctx

        memScoped {
            val rc = WinOpenSSLBindings.SSL_CTX_set_cipher_list?.invoke(ctx, "ALL:@SECLEVEL=0".cstr.ptr) ?: 0
            if (rc == 0) {
                WinOpenSSLBindings.SSL_CTX_set_cipher_list?.invoke(ctx, "ALL".cstr.ptr)
            }
        }

        if (trustManager == null || trustManager is SystemX509TrustManager) {
            WinOpenSSLBindings.SSL_CTX_set_default_verify_paths?.invoke(ctx)
        }

        val ssl = WinOpenSSLBindings.SSL_new?.invoke(ctx) ?: run {
            WinOpenSSLBindings.SSL_CTX_free?.invoke(ctx)
            sslCtxPtr = null
            throw SSLException("Failed to create SSL object")
        }
        sslPtr = ssl

        val sockHandle = underlyingSocket.socketHandle
        if (sockHandle == INVALID_SOCKET.toULong()) {
            close()
            throw SSLException("Invalid socket handle")
        }

        WinOpenSSLBindings.SSL_set_fd?.invoke(ssl, sockHandle.toInt())

        if (peerHostName.isNotEmpty()) {
            memScoped {
                val hostCStr = peerHostName.cstr.ptr
                WinOpenSSLBindings.SSL_ctrl?.invoke(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name, hostCStr)
            }
        }

        val ret = WinOpenSSLBindings.SSL_connect?.invoke(ssl) ?: -1
        if (ret <= 0) {
            close()
            throw SSLHandshakeException("OpenSSL TLS handshake failed: code $ret")
        }

        val certPtr = WinOpenSSLBindings.SSL_get_peer_certificate?.invoke(ssl)
        if (certPtr != null) {
            try {
                val len = WinOpenSSLBindings.i2d_X509?.invoke(certPtr, null) ?: -1
                if (len > 0) {
                    memScoped {
                        val derBuf = allocArray<ByteVar>(len)
                        val outPtr = alloc<CPointerVar<ByteVar>>()
                        outPtr.value = derBuf
                        val written = WinOpenSSLBindings.i2d_X509?.invoke(certPtr, outPtr.ptr.reinterpret()) ?: -1
                        if (written > 0) {
                            val bytes = ByteArray(written) { i -> derBuf[i] }
                            val certFactory = CertificateFactory.getInstance("X.509")
                            val cert = certFactory.generateCertificate(ByteArrayInputStream(bytes))
                            peerCerts = arrayOf(cert)
                        }
                    }
                }
            } finally {
                WinOpenSSLBindings.X509_free?.invoke(certPtr)
            }
        }

        val tm = trustManager
        if (tm != null && tm !is SystemX509TrustManager) {
            val x509Chain = peerCerts.filterIsInstance<X509Certificate>().toTypedArray()
            try {
                tm.checkServerTrusted(x509Chain, "RSA")
            } catch (e: Exception) {
                close()
                throw SSLHandshakeException(e.message ?: "Certificate verification failed").apply {
                    initCause(e)
                }
            }
        } else {
            val verifyRes = WinOpenSSLBindings.SSL_get_verify_result?.invoke(ssl) ?: 0
            if (peerCerts.isEmpty() || verifyRes != 0) {
                close()
                throw SSLHandshakeException("OpenSSL server certificate verification failed: result=$verifyRes")
            }
        }

        WinOpenSSLBindings.SSL_get_version?.invoke(ssl)?.let { verPtr ->
            negotiatedProtocol = verPtr.toKString()
        }
        WinOpenSSLBindings.SSL_get_current_cipher?.invoke(ssl)?.let { cipherPtr ->
            WinOpenSSLBindings.SSL_CIPHER_get_name?.invoke(cipherPtr)?.let { namePtr ->
                negotiatedCipherSuite = namePtr.toKString()
            }
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
            var totalWritten = 0
            while (totalWritten < len) {
                val written = WinOpenSSLBindings.SSL_write?.invoke(ssl, buf + totalWritten, len - totalWritten) ?: -1
                if (written <= 0) {
                    throw SSLException("OpenSSL write failed: $written")
                }
                totalWritten += written
            }
        }
    }

    override fun getInputStream(): InputStream = sslInputStream
    override fun getOutputStream(): OutputStream = sslOutputStream

    override fun getSession(): SSLSession {
        if (!isHandshakeDone && !isClosedSocket) {
            try {
                startHandshake()
            } catch (_: Exception) {
            }
        }
        return NativeSSLSession(
            host = peerHostName,
            port = getPort(),
            peerCerts = peerCerts,
            negotiatedProtocol = negotiatedProtocol,
            negotiatedCipherSuite = negotiatedCipherSuite,
            sessionValid = isHandshakeDone && !isClosedSocket
        )
    }

    override fun isClosed(): Boolean = isClosedSocket || underlyingSocket.isClosed()

    override fun close() {
        if (!isClosedSocket) {
            isClosedSocket = true
            val ssl = sslPtr
            if (ssl != null) {
                if (isHandshakeDone) {
                    WinOpenSSLBindings.SSL_shutdown?.invoke(ssl)
                }
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
        if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        if (len == 0) return 0
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
        if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        if (len == 0) return
        sslSocket.performWrite(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}
