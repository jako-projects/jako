@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.UnsafeNumber::class)

package javax.net.ssl

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Security.*
import platform.darwin.OSStatus
import platform.posix.*

private const val ERR_SSL_PARAM: OSStatus = -9801
private const val ERR_SSL_WOULD_BLOCK: OSStatus = -9803
private const val ERR_SSL_CLOSED_GRACEFUL: OSStatus = -9805
private const val ERR_SSL_CLOSED_ABORT: OSStatus = -9806
private const val ERR_SSL_SERVER_AUTH_COMPLETED: OSStatus = -9841
private const val NO_ERR: OSStatus = 0

internal actual fun createPlatformSSLSocket(
    s: Socket,
    host: String,
    port: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager?
): SSLSocket {
    return AppleSSLSocket(s, host, port, autoClose, trustManager)
}

internal class AppleSSLSocket(
    underlyingSocket: Socket,
    host: String,
    peerPort: Int,
    autoClose: Boolean,
    trustManager: X509TrustManager? = null
) : DelegatingSSLSocket(underlyingSocket, host, peerPort, autoClose, trustManager) {

    private var contextRef: SSLContextRef? = null
    private var isHandshakeDone = false
    private var isClosedSocket = false
    private var peerCerts: Array<Certificate> = emptyArray()
    private var negotiatedProtocol: String = "TLSv1.2"
    private var negotiatedCipherSuite: String = "TLS_AES_128_GCM_SHA256"

    private val sslInputStream: InputStream by lazy {
        AppleSSLInputStream(this)
    }

    private val sslOutputStream: OutputStream by lazy {
        AppleSSLOutputStream(this)
    }

    private fun initSSLIfNeeded(): SSLContextRef {
        contextRef?.let { return it }
        signal(SIGPIPE, SIG_IGN)
        ensureUnderlyingConnected()

        val side = if (clientMode) SSLProtocolSide.kSSLClientSide else SSLProtocolSide.kSSLServerSide
        val ctx = SSLCreateContext(kCFAllocatorDefault, side, SSLConnectionType.kSSLStreamType)
            ?: throw SSLException("Failed to create Apple SSLContext")
        contextRef = ctx

        if (peerHostName.isNotEmpty()) {
            SSLSetPeerDomainName(ctx, peerHostName, peerHostName.length.convert())
        }

        if (trustManager != null && trustManager !is SystemX509TrustManager) {
            SSLSetSessionOption(ctx, kSSLSessionOptionBreakOnServerAuth, true)
        }

        val fd = underlyingSocket.fd
        if (fd < 0) {
            throw SSLException("Underlying socket is closed or invalid")
        }
        val connectionRef = fd.toLong().toCPointer<ByteVar>()
        SSLSetConnection(ctx, connectionRef)

        val readFunc = staticCFunction(::appleSslReadCallback)
        val writeFunc = staticCFunction(::appleSslWriteCallback)
        SSLSetIOFuncs(ctx, readFunc, writeFunc)
        return ctx
    }

    override fun startHandshake() {
        if (isHandshakeDone) return
        if (isClosedSocket) throw SocketException("Socket is closed")
        val ctx = initSSLIfNeeded()

        while (true) {
            val status = SSLHandshake(ctx)
            when (status) {
                NO_ERR -> {
                    if (peerCerts.isEmpty()) {
                        peerCerts = extractPeerCertificates(ctx)
                    }
                    updateNegotiatedParameters(ctx)
                    isHandshakeDone = true
                    break
                }
                ERR_SSL_WOULD_BLOCK -> continue
                ERR_SSL_SERVER_AUTH_COMPLETED -> {
                    peerCerts = extractPeerCertificates(ctx)
                    val tm = trustManager
                    if (tm != null) {
                        val x509Chain = peerCerts.filterIsInstance<X509Certificate>().toTypedArray()
                        try {
                            tm.checkServerTrusted(x509Chain, "RSA")
                        } catch (e: Exception) {
                            close()
                            throw SSLHandshakeException(e.message ?: "Server certificate verification failed").apply {
                                initCause(e)
                            }
                        }
                    }
                    continue
                }
                else -> {
                    close()
                    throw SSLHandshakeException("Apple SSLHandshake failed with OSStatus $status")
                }
            }
        }
    }

    private fun updateNegotiatedParameters(ctx: SSLContextRef) {
        memScoped {
            val protoVar = alloc<SSLProtocolVar>()
            if (SSLGetNegotiatedProtocolVersion(ctx, protoVar.ptr) == NO_ERR) {
                val protoCode = protoVar.ptr.reinterpret<IntVar>().pointed.value
                negotiatedProtocol = when (protoCode) {
                    10 -> "TLSv1.3"
                    8 -> "TLSv1.2"
                    7 -> "TLSv1.1"
                    4 -> "TLSv1"
                    else -> "TLSv1.2"
                }
            }
            val cipherVar = alloc<SSLCipherSuiteVar>()
            if (SSLGetNegotiatedCipher(ctx, cipherVar.ptr) == NO_ERR) {
                negotiatedCipherSuite = mapCipherSuiteCode(cipherVar.value.toInt() and 0xFFFF)
            }
        }
    }

    private fun mapCipherSuiteCode(code: Int): String = when (code) {
        0x1301 -> "TLS_AES_128_GCM_SHA256"
        0x1302 -> "TLS_AES_256_GCM_SHA384"
        0x1303 -> "TLS_CHACHA20_POLY1305_SHA256"
        0xC02B -> "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"
        0xC02C -> "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"
        0xC02F -> "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
        0xC030 -> "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
        0x009C -> "TLS_RSA_WITH_AES_128_GCM_SHA256"
        0x009D -> "TLS_RSA_WITH_AES_256_GCM_SHA384"
        0x003C -> "TLS_RSA_WITH_AES_128_CBC_SHA256"
        0x003D -> "TLS_RSA_WITH_AES_256_CBC_SHA256"
        0x002F -> "TLS_RSA_WITH_AES_128_CBC_SHA"
        0x0035 -> "TLS_RSA_WITH_AES_256_CBC_SHA"
        else -> "TLS_CIPHER_0x${code.toString(16).uppercase()}"
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
                        val dataRef = SecCertificateCopyData(certRef)
                        if (dataRef != null) {
                            val ptr = CFDataGetBytePtr(dataRef)
                            val length = CFDataGetLength(dataRef).toInt()
                            if (ptr != null && length > 0) {
                                val bytes = ByteArray(length)
                                for (j in 0 until length) {
                                    bytes[j] = ptr[j].toByte()
                                }
                                certList.add(factory.generateCertificate(bytes))
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

            while (true) {
                val status = SSLRead(ctx, buf, len.convert(), processed.ptr)
                val bytesRead = processed.value.toInt()

                if (bytesRead > 0 && (status == NO_ERR || status == ERR_SSL_WOULD_BLOCK || status == ERR_SSL_CLOSED_GRACEFUL)) {
                    for (i in 0 until bytesRead) {
                        b[off + i] = buf[i]
                    }
                    return@memScoped bytesRead
                } else if (status == ERR_SSL_WOULD_BLOCK) {
                    continue
                } else if (status == ERR_SSL_CLOSED_GRACEFUL || status == ERR_SSL_CLOSED_ABORT || bytesRead == 0) {
                    return@memScoped -1
                } else {
                    throw SSLException("SSLRead failed with status $status")
                }
            }
            -1
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
            contextRef?.let { ctx ->
                if (isHandshakeDone) {
                    SSLClose(ctx)
                }
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

    val dst = data.reinterpret<ByteVar>()
    var totalRead = 0
    while (totalRead < requested) {
        val n = recv(socketFd, dst + totalRead, (requested - totalRead).convert(), 0).toInt()
        if (n > 0) {
            totalRead += n
        } else if (n == 0) {
            len.pointed.value = totalRead.convert()
            return if (totalRead > 0) ERR_SSL_WOULD_BLOCK else ERR_SSL_CLOSED_GRACEFUL
        } else {
            val err = errno
            if (err == EINTR) continue
            len.pointed.value = totalRead.convert()
            return if (err == EAGAIN || err == EWOULDBLOCK || totalRead > 0) ERR_SSL_WOULD_BLOCK else ERR_SSL_CLOSED_ABORT
        }
    }
    len.pointed.value = totalRead.convert()
    return NO_ERR
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

    val src = data.reinterpret<ByteVar>()
    var totalWritten = 0
    while (totalWritten < requested) {
        val n = send(socketFd, src + totalWritten, (requested - totalWritten).convert(), 0).toInt()
        if (n > 0) {
            totalWritten += n
        } else if (n == 0) {
            len.pointed.value = totalWritten.convert()
            return ERR_SSL_WOULD_BLOCK
        } else {
            val err = errno
            if (err == EINTR) continue
            len.pointed.value = totalWritten.convert()
            return if (err == EAGAIN || err == EWOULDBLOCK || totalWritten > 0) ERR_SSL_WOULD_BLOCK else ERR_SSL_CLOSED_ABORT
        }
    }
    len.pointed.value = totalWritten.convert()
    return NO_ERR
}

internal class AppleSSLInputStream(private val sslSocket: AppleSSLSocket) : InputStream() {
    override fun read(): Int {
        val b = ByteArray(1)
        val n = sslSocket.readSSL(b, 0, 1)
        if (n <= 0) return -1
        return b[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        if (len == 0) return 0
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
        if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
        if (len == 0) return
        sslSocket.writeSSL(b, off, len)
    }

    override fun close() {
        sslSocket.close()
    }
}
