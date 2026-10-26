package javax.net.ssl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.Socket
import java.net.URL
import java.security.Principal
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Collections

internal object DefaultHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String, session: SSLSession): Boolean {
        val certs = try {
            session.getPeerCertificates()
        } catch (_: SSLPeerUnverifiedException) {
            return false
        }
        val leaf = certs.firstOrNull() as? X509Certificate ?: return false
        val target = hostname.trim().lowercase()
        if (target.isEmpty()) return false

        val isIpLiteral = target.all { it.isDigit() || it == '.' } || target.contains(':')
        val sans = leaf.getSubjectAlternativeNames()
        var hasDnsSan = false

        if (sans != null) {
            for (entry in sans) {
                if (entry.size < 2) continue
                val type = entry[0] as? Int ?: continue
                val value = (entry[1] as? String)?.trim()?.lowercase() ?: continue
                if (isIpLiteral && type == 7) {
                    if (value == target) return true
                } else if (!isIpLiteral && type == 2) {
                    hasDnsSan = true
                    if (matchesDnsPattern(target, value)) return true
                }
            }
            if (isIpLiteral || hasDnsSan) return false
        }

        // Fallback to CN in Subject DN when no dNSName SANs exist
        val cn = extractCommonName(leaf.getSubjectX500Principal().getName())?.lowercase() ?: return false
        return matchesDnsPattern(target, cn)
    }

    private fun matchesDnsPattern(hostname: String, pattern: String): Boolean {
        if (hostname == pattern) return true
        if (pattern.startsWith("*.") && pattern.length > 2) {
            val suffix = pattern.substring(1) // ".example.com"
            if (hostname.endsWith(suffix)) {
                val prefix = hostname.substring(0, hostname.length - suffix.length)
                if (prefix.isNotEmpty() && !prefix.contains('.')) {
                    return true
                }
            }
        }
        return false
    }

    private fun extractCommonName(dn: String): String? {
        for (part in dn.split(',')) {
            val trimmed = part.trim()
            if (trimmed.startsWith("CN=", ignoreCase = true)) {
                return trimmed.substring(3).trim()
            }
        }
        return null
    }
}

actual abstract class HttpsURLConnection protected actual constructor(url: URL?) : HttpURLConnection(url) {
    private var _hostnameVerifier: HostnameVerifier = defaultHostnameVerifierInstance
    private var _sslSocketFactory: SSLSocketFactory = defaultSSLSocketFactoryInstance

    actual abstract fun getCipherSuite(): String
    actual open val cipherSuite: String get() = getCipherSuite()

    actual abstract fun getLocalCertificates(): Array<Certificate>?
    actual open val localCertificates: Array<Certificate>? get() = getLocalCertificates()

    actual abstract fun getServerCertificates(): Array<Certificate>
    actual open val serverCertificates: Array<Certificate> get() = getServerCertificates()

    actual open fun getPeerPrincipal(): Principal {
        val certs = getServerCertificates()
        val x509 = certs.firstOrNull() as? X509Certificate
            ?: throw SSLPeerUnverifiedException("No X509 server certificate")
        return x509.getSubjectX500Principal()
    }
    actual open val peerPrincipal: Principal get() = getPeerPrincipal()

    actual open fun getLocalPrincipal(): Principal? {
        val certs = getLocalCertificates() ?: return null
        return (certs.firstOrNull() as? X509Certificate)?.getSubjectX500Principal()
    }
    actual open val localPrincipal: Principal? get() = getLocalPrincipal()

    actual open fun setHostnameVerifier(v: HostnameVerifier) {
        _hostnameVerifier = v
    }

    actual open fun getHostnameVerifier(): HostnameVerifier = _hostnameVerifier

    actual open var hostnameVerifier: HostnameVerifier
        get() = getHostnameVerifier()
        set(value) = setHostnameVerifier(value)

    actual open fun setSSLSocketFactory(sf: SSLSocketFactory) {
        _sslSocketFactory = sf
    }

    actual open fun getSSLSocketFactory(): SSLSocketFactory = _sslSocketFactory

    actual open var sslSocketFactory: SSLSocketFactory
        get() = getSSLSocketFactory()
        set(value) = setSSLSocketFactory(value)

    actual companion object {
        private var defaultHostnameVerifierInstance: HostnameVerifier = DefaultHostnameVerifier
        private var defaultSSLSocketFactoryInstance: SSLSocketFactory = SSLSocketFactory.getDefault()

        actual fun getDefaultHostnameVerifier(): HostnameVerifier = defaultHostnameVerifierInstance
        actual fun setDefaultHostnameVerifier(v: HostnameVerifier) {
            defaultHostnameVerifierInstance = v
        }

        actual fun getDefaultSSLSocketFactory(): SSLSocketFactory = defaultSSLSocketFactoryInstance
        actual fun setDefaultSSLSocketFactory(sf: SSLSocketFactory) {
            defaultSSLSocketFactoryInstance = sf
        }
    }
}

internal class SocketHttpsURLConnection(url: URL) : HttpsURLConnection(url) {
    private var sslSocket: SSLSocket? = null
    private var negotiatedSession: SSLSession? = null
    private val requestBodyBuffer = ByteArrayOutputStream()
    private var executed = false
    private val responseHeaders = LinkedHashMap<String?, MutableList<String>>()
    private val headerOrder = mutableListOf<Pair<String?, String>>()
    private var responseBodyBytes: ByteArray = ByteArray(0)
    private var responseBodyStream: InputStream? = null
    private var errorBodyStream: InputStream? = null

    override fun connect() {
        if (connected) return
        val u = url ?: throw MalformedURLException("null URL")
        val host = u.getHost() ?: throw MalformedURLException("Missing host in URL: $u")
        val port = if (u.getPort() > 0) u.getPort() else 443

        val rawSocket = Socket()
        if (connectTimeout > 0) {
            rawSocket.connect(InetSocketAddress(host, port), connectTimeout)
        } else {
            rawSocket.connect(InetSocketAddress(host, port))
        }
        if (readTimeout > 0) {
            rawSocket.setSoTimeout(readTimeout)
        }

        val wrapped = getSSLSocketFactory().createSocket(rawSocket, host, port, true) as SSLSocket
        wrapped.startHandshake()
        val session = wrapped.getSession()
        if (!getHostnameVerifier().verify(host, session)) {
            wrapped.close()
            throw SSLPeerUnverifiedException("Hostname '$host' was not verified")
        }
        sslSocket = wrapped
        negotiatedSession = session
        connected = true
    }

    override fun getCipherSuite(): String {
        val s = negotiatedSession ?: throw IllegalStateException("connection not yet open")
        return s.getCipherSuite()
    }

    override fun getLocalCertificates(): Array<Certificate>? {
        val s = negotiatedSession ?: throw IllegalStateException("connection not yet open")
        return s.getLocalCertificates()
    }

    override fun getServerCertificates(): Array<Certificate> {
        val s = negotiatedSession ?: throw IllegalStateException("connection not yet open")
        return s.getPeerCertificates()
    }

    override fun disconnect() {
        connected = false
        try {
            sslSocket?.close()
        } catch (_: Exception) {
        }
        sslSocket = null
    }

    override fun usingProxy(): Boolean = false

    override fun getOutputStream(): OutputStream {
        if (!doOutput) {
            throw ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)")
        }
        if (method == "GET") {
            method = "POST"
        }
        return requestBodyBuffer
    }

    override fun getInputStream(): InputStream {
        executeIfNeeded()
        if (responseCodeValue >= 400) {
            throw IOException("Server returned HTTP response code: $responseCodeValue for URL: $url")
        }
        return responseBodyStream ?: ByteArrayInputStream(responseBodyBytes)
    }

    override fun getErrorStream(): InputStream? {
        try {
            executeIfNeeded()
        } catch (_: IOException) {
        }
        return errorBodyStream
    }

    override fun getHeaderField(name: String): String? {
        try {
            executeIfNeeded()
        } catch (_: IOException) {
        }
        for ((k, v) in responseHeaders) {
            if (k != null && k.equals(name, ignoreCase = true)) {
                return v.lastOrNull()
            }
        }
        return null
    }

    override fun getHeaderField(n: Int): String? {
        try {
            executeIfNeeded()
        } catch (_: IOException) {
        }
        return headerOrder.getOrNull(n)?.second
    }

    override fun getHeaderFieldKey(n: Int): String? {
        try {
            executeIfNeeded()
        } catch (_: IOException) {
        }
        return headerOrder.getOrNull(n)?.first
    }

    override fun getHeaderFields(): Map<String?, List<String>> {
        try {
            executeIfNeeded()
        } catch (_: IOException) {
        }
        val copy = LinkedHashMap<String?, List<String>>()
        for ((k, v) in responseHeaders) {
            copy[k] = v.toList()
        }
        return Collections.unmodifiableMap(copy)
    }

    private fun executeIfNeeded() {
        if (executed) return
        connect()
        executed = true
        val s = sslSocket ?: throw IOException("SSLSocket not connected")
        val u = url!!
        val host = u.getHost() ?: "localhost"
        val port = u.getPort()
        val hostHeader = if (port > 0 && port != 443) "$host:$port" else host
        val pathAndQuery = u.getFile().ifEmpty { "/" }
        val bodyBytes = requestBodyBuffer.toByteArray()

        val req = StringBuilder()
        req.append(method).append(' ').append(pathAndQuery).append(" HTTP/1.1\r\n")
        if (findRequestProperty("Host") == null) {
            req.append("Host: ").append(hostHeader).append("\r\n")
        }
        if (findRequestProperty("Connection") == null) {
            req.append("Connection: close\r\n")
        }
        if (bodyBytes.isNotEmpty() && findRequestProperty("Content-Length") == null) {
            req.append("Content-Length: ").append(bodyBytes.size).append("\r\n")
        }
        for ((k, values) in requestPropertiesMap) {
            for (v in values) {
                req.append(k).append(": ").append(v).append("\r\n")
            }
        }
        req.append("\r\n")

        val out = s.getOutputStream()
        out.write(req.toString().encodeToByteArray())
        if (bodyBytes.isNotEmpty()) {
            out.write(bodyBytes)
        }
        out.flush()

        val inp = s.getInputStream()
        val statusLine = readHttpLine(inp) ?: throw IOException("Unexpected EOF reading HTTPS status line")
        headerOrder.add(Pair(null, statusLine))
        responseHeaders.getOrPut(null) { mutableListOf() }.add(statusLine)

        val parts = statusLine.split(' ', limit = 3)
        if (parts.size >= 2) {
            responseCodeValue = parts[1].toIntOrNull() ?: -1
            responseMessageValue = if (parts.size >= 3) parts[2] else ""
        }

        while (true) {
            val line = readHttpLine(inp) ?: break
            if (line.isEmpty()) break
            val colon = line.indexOf(':')
            if (colon > 0) {
                val key = line.substring(0, colon).trim()
                val value = line.substring(colon + 1).trim()
                headerOrder.add(Pair(key, value))
                responseHeaders.getOrPut(key) { mutableListOf() }.add(value)
            }
        }

        val transferEncoding = responseHeaders.entries.firstOrNull { it.key.equals("Transfer-Encoding", ignoreCase = true) }?.value?.lastOrNull()
        val contentLengthHdr = responseHeaders.entries.firstOrNull { it.key.equals("Content-Length", ignoreCase = true) }?.value?.lastOrNull()?.toIntOrNull()

        responseBodyBytes = if (method == "HEAD" || responseCodeValue == 204 || responseCodeValue == 304) {
            ByteArray(0)
        } else if (transferEncoding != null && transferEncoding.contains("chunked", ignoreCase = true)) {
            readChunkedBody(inp)
        } else if (contentLengthHdr != null && contentLengthHdr >= 0) {
            readFixedBytes(inp, contentLengthHdr)
        } else {
            inp.readBytes()
        }

        try {
            s.close()
        } catch (_: Exception) {
        }

        if (responseCodeValue >= 400) {
            errorBodyStream = ByteArrayInputStream(responseBodyBytes)
        } else {
            responseBodyStream = ByteArrayInputStream(responseBodyBytes)
        }
    }

    private fun readHttpLine(inp: InputStream): String? {
        val baos = ByteArrayOutputStream()
        var sawByte = false
        while (true) {
            val b = inp.read()
            if (b < 0) break
            sawByte = true
            if (b == '\n'.code) break
            if (b != '\r'.code) baos.write(b)
        }
        if (!sawByte && baos.size() == 0) return null
        return baos.toByteArray().decodeToString()
    }

    private fun readFixedBytes(inp: InputStream, length: Int): ByteArray {
        val buf = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val n = inp.read(buf, offset, length - offset)
            if (n <= 0) break
            offset += n
        }
        return if (offset == length) buf else buf.copyOf(offset)
    }

    private fun readChunkedBody(inp: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        while (true) {
            val sizeLine = readHttpLine(inp)?.trim() ?: break
            val semi = sizeLine.indexOf(';')
            val hexStr = if (semi >= 0) sizeLine.substring(0, semi).trim() else sizeLine
            val chunkSize = hexStr.toIntOrNull(16) ?: break
            if (chunkSize == 0) {
                readHttpLine(inp)
                break
            }
            val chunk = readFixedBytes(inp, chunkSize)
            out.write(chunk)
            readHttpLine(inp)
        }
        return out.toByteArray()
    }
}
