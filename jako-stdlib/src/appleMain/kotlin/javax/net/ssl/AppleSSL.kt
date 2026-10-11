@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.UnsafeNumber::class)

package javax.net.ssl

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Security.*
import platform.darwin.OSStatus
import platform.posix.*

private const val ERR_SSL_PARAM: OSStatus = -9801
private const val ERR_SSL_WOULD_BLOCK: OSStatus = -9803
private const val ERR_SSL_CLOSED_GRACEFUL: OSStatus = -9805
private const val ERR_SSL_CLOSED_ABORT: OSStatus = -9806
private const val NO_ERR: OSStatus = 0

internal actual fun createPlatformSSLSocket(s: Socket, host: String, port: Int, autoClose: Boolean): SSLSocket {
    return AppleSSLSocket(s, host, port, autoClose)
}

internal class AppleSSLSocket(
    private val underlyingSocket: Socket,
    private val host: String,
    private val peerPort: Int,
    private val autoClose: Boolean
) : SSLSocket() {

    private var contextRef: SSLContextRef? = null
    private var isHandshakeDone = false
    private var isClosedSocket = false
    private var peerCerts: Array<Certificate> = emptyArray()

    private var clientMode = true
    private var needAuth = false
    private var wantAuth = false
    private var protocols = arrayOf("TLSv1.2", "TLSv1.3")
    private var cipherSuites = arrayOf(
        "TLS_AES_128_GCM_SHA256",
        "TLS_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
    )

    private val sslInputStream: InputStream by lazy {
        AppleSSLInputStream(this)
    }

    private val sslOutputStream: OutputStream by lazy {
        AppleSSLOutputStream(this)
    }

    init {
        initSSL()
    }

    private fun initSSL() {
        val ctx = SSLCreateContext(kCFAllocatorDefault, SSLProtocolSide.kSSLClientSide, SSLConnectionType.kSSLStreamType)
            ?: throw SSLException("Failed to create Apple SSLContext")
        contextRef = ctx

        // Set SNI
        if (host.isNotEmpty()) {
            SSLSetPeerDomainName(ctx, host, host.length.convert())
        }

        // Set Connection reference (pass underlyingSocket.fd)
        val fd = underlyingSocket.fd
        check(fd >= 0) { "Underlying socket is closed or invalid" }
        val connectionRef = fd.toLong().toCPointer<ByteVar>()
        SSLSetConnection(ctx, connectionRef)

        val readFunc = staticCFunction(::appleSslReadCallback)
        val writeFunc = staticCFunction(::appleSslWriteCallback)

        SSLSetIOFuncs(ctx, readFunc, writeFunc)
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        val ctx = contextRef ?: throw SSLException("SSLContext is null")

        while (true) {
            val status = SSLHandshake(ctx)
            if (status == NO_ERR) {
                isHandshakeDone = true
                peerCerts = extractPeerCertificates(ctx)
                break
            } else if (status == ERR_SSL_WOULD_BLOCK) {
                continue
            } else {
                throw SSLHandshakeException("Apple SSLHandshake failed with OSStatus $status")
            }
        }
    }

    private fun extractPeerCertificates(ctx: SSLContextRef): Array<Certificate> {
        return memScoped {
            val trustRef = alloc<SecTrustRefVar>()
            if (SSLCopyPeerTrust(ctx, trustRef.ptr) == NO_ERR && trustRef.value != null) {
                val trust = trustRef.value!!
                val count = SecTrustGetCertificateCount(trust).toInt()
                val certList = mutableListOf<Certificate>()
                val factory = CertificateFactory.getInstance("X.509")

                for (i in 0 until count) {
                    val certRef = SecTrustGetCertificateAtIndex(trust, i.convert())
                    if (certRef != null) {
                        val dnsNames = mutableListOf<String>()
                        if (host.isNotEmpty()) dnsNames.add(host)

                        val summaryRef = SecCertificateCopySubjectSummary(certRef)
                        if (summaryRef != null) {
                            val ptr = CFStringGetCStringPtr(summaryRef, kCFStringEncodingUTF8)
                            if (ptr != null) {
                                val summaryStr = ptr.toKString()
                                if (summaryStr.isNotEmpty() && !dnsNames.contains(summaryStr)) {
                                    dnsNames.add(summaryStr)
                                }
                            } else {
                                val buf = allocArray<ByteVar>(256)
                                if (CFStringGetCString(summaryRef, buf, 256.convert(), kCFStringEncodingUTF8)) {
                                    val summaryStr = buf.toKString()
                                    if (summaryStr.isNotEmpty() && !dnsNames.contains(summaryStr)) {
                                        dnsNames.add(summaryStr)
                                    }
                                }
                            }
                            CFRelease(summaryRef)
                        }

                        val dataRef = SecCertificateCopyData(certRef)
                        if (dataRef != null) {
                            val ptr = CFDataGetBytePtr(dataRef)
                            val length = CFDataGetLength(dataRef).toInt()
                            if (ptr != null && length > 0) {
                                val bytes = ByteArray(length)
                                for (j in 0 until length) {
                                    bytes[j] = ptr[j].toByte()
                                }
                                certList.add(factory.generateCertificate(bytes, dnsNames))
                            }
                            CFRelease(dataRef)
                        }
                    }
                }
                CFRelease(trust)
                certList.toTypedArray()
            } else {
                emptyArray()
            }
        }
    }

    internal fun readSSL(b: ByteArray, off: Int, len: Int): Int {
        if (!isHandshakeDone) startHandshake()
        val ctx = contextRef ?: throw SSLException("SSLContext is null")
        if (len == 0) return 0

        return memScoped {
            val buf = allocArray<ByteVar>(len)
            val processed = alloc<size_tVar>()

            val status = SSLRead(ctx, buf, len.convert(), processed.ptr)
            val bytesRead = processed.value.toInt()

            if (status == NO_ERR || (status == ERR_SSL_CLOSED_GRACEFUL && bytesRead > 0)) {
                for (i in 0 until bytesRead) {
                    b[off + i] = buf[i]
                }
                bytesRead
            } else if (status == ERR_SSL_CLOSED_GRACEFUL || bytesRead == 0) {
                -1
            } else {
                throw SSLException("SSLRead failed with status $status")
            }
        }
    }

    internal fun writeSSL(b: ByteArray, off: Int, len: Int) {
        if (!isHandshakeDone) startHandshake()
        val ctx = contextRef ?: throw SSLException("SSLContext is null")
        if (len == 0) return

        memScoped {
            val buf = allocArray<ByteVar>(len)
            for (i in 0 until len) {
                buf[i] = b[off + i]
            }
            val processed = alloc<size_tVar>()
            var totalWritten = 0

            while (totalWritten < len) {
                val toWrite = len - totalWritten
                val status = SSLWrite(ctx, buf + totalWritten, toWrite.convert(), processed.ptr)
                val written = processed.value.toInt()
                if (status == NO_ERR || status == ERR_SSL_WOULD_BLOCK) {
                    totalWritten += written
                } else {
                    throw SSLException("SSLWrite failed with status $status")
                }
            }
        }
    }

    override fun getInputStream(): InputStream = sslInputStream

    override fun getOutputStream(): OutputStream = sslOutputStream

    override fun getSession(): SSLSession = NativeSSLSession(host, port, peerCerts)

    override fun setUseClientMode(mode: Boolean) {
        clientMode = mode
    }

    override fun getUseClientMode(): Boolean = clientMode

    override fun setNeedClientAuth(need: Boolean) {
        needAuth = need
    }

    override fun setWantClientAuth(want: Boolean) {
        wantAuth = want
    }

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

    override fun close() {
        if (!isClosedSocket) {
            isClosedSocket = true
            contextRef?.let { ctx ->
                SSLClose(ctx)
                CFRelease(ctx)
            }
            contextRef = null
            if (autoClose) {
                underlyingSocket.close()
            }
        }
    }
}

private fun appleSslReadCallback(
    conn: SSLConnectionRef?,
    data: CPointer<out CPointed>?,
    len: CPointer<size_tVar>?
): OSStatus {
    if (conn == null || data == null || len == null) return ERR_SSL_PARAM
    val socketFd = conn.toLong().toInt()
    val requested = len.pointed.value.toInt()
    if (requested == 0) return NO_ERR

    val read = recv(socketFd, data, requested.convert(), 0).toInt()
    return if (read > 0) {
        len.pointed.value = read.convert()
        NO_ERR
    } else if (read == 0) {
        len.pointed.value = 0.convert()
        ERR_SSL_CLOSED_GRACEFUL
    } else {
        val err = errno
        len.pointed.value = 0.convert()
        if (err == EAGAIN || err == EWOULDBLOCK) ERR_SSL_WOULD_BLOCK else ERR_SSL_CLOSED_ABORT
    }
}

private fun appleSslWriteCallback(
    conn: SSLConnectionRef?,
    data: CPointer<out CPointed>?,
    len: CPointer<size_tVar>?
): OSStatus {
    if (conn == null || data == null || len == null) return ERR_SSL_PARAM
    val socketFd = conn.toLong().toInt()
    val requested = len.pointed.value.toInt()
    if (requested == 0) return NO_ERR

    val written = send(socketFd, data, requested.convert(), 0).toInt()
    return if (written >= 0) {
        len.pointed.value = written.convert()
        NO_ERR
    } else {
        val err = errno
        len.pointed.value = 0.convert()
        if (err == EAGAIN || err == EWOULDBLOCK) ERR_SSL_WOULD_BLOCK else ERR_SSL_CLOSED_ABORT
    }
}

internal class AppleSSLInputStream(private val sslSocket: AppleSSLSocket) : InputStream() {
    override fun read(): Int {
        val b = ByteArray(1)
        val n = sslSocket.readSSL(b, 0, 1)
        if (n <= 0) return -1
        return b[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return sslSocket.readSSL(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}

internal class AppleSSLOutputStream(private val sslSocket: AppleSSLSocket) : OutputStream() {
    override fun write(b: Int) {
        val buf = byteArrayOf(b.toByte())
        sslSocket.writeSSL(buf, 0, 1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        sslSocket.writeSSL(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}
