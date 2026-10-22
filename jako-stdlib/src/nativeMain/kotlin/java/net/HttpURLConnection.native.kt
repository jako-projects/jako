package java.net

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections

actual abstract class HttpURLConnection protected actual constructor(url: URL?) : URLConnection(url) {
    protected var method: String = "GET"
    protected var responseCodeValue: Int = -1
    protected var responseMessageValue: String? = null
    protected var instanceFollowRedirectsValue: Boolean = true

    actual abstract fun disconnect()
    actual abstract fun usingProxy(): Boolean

    actual open fun setRequestMethod(method: String) {
        val upper = method.uppercase()
        if (upper !in VALID_METHODS) {
            throw ProtocolException("Invalid HTTP method: $method")
        }
        this.method = upper
    }

    actual open fun getRequestMethod(): String = method

    actual open var requestMethod: String
        get() = getRequestMethod()
        set(value) = setRequestMethod(value)

    actual open fun setInstanceFollowRedirects(followRedirects: Boolean) {
        instanceFollowRedirectsValue = followRedirects
    }

    actual open fun getInstanceFollowRedirects(): Boolean = instanceFollowRedirectsValue

    actual open var instanceFollowRedirects: Boolean
        get() = getInstanceFollowRedirects()
        set(value) = setInstanceFollowRedirects(value)

    actual open fun getResponseCode(): Int {
        getInputStreamOrExecute()
        return responseCodeValue
    }

    actual open fun getResponseMessage(): String? {
        getInputStreamOrExecute()
        return responseMessageValue
    }

    actual open val responseCode: Int get() = getResponseCode()
    actual open val responseMessage: String? get() = getResponseMessage()

    actual open fun getErrorStream(): InputStream? = null
    actual open val errorStream: InputStream? get() = getErrorStream()

    protected open fun getInputStreamOrExecute() {
        try {
            getInputStream()
        } catch (e: IOException) {
            if (responseCodeValue == -1) throw e
        }
    }

    actual companion object {
        private val VALID_METHODS = setOf("GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE")

        actual const val HTTP_OK = 200
        actual const val HTTP_CREATED = 201
        actual const val HTTP_ACCEPTED = 202
        actual const val HTTP_NOT_AUTHORITATIVE = 203
        actual const val HTTP_NO_CONTENT = 204
        actual const val HTTP_RESET = 205
        actual const val HTTP_PARTIAL = 206
        actual const val HTTP_MULT_CHOICE = 300
        actual const val HTTP_MOVED_PERM = 301
        actual const val HTTP_MOVED_TEMP = 302
        actual const val HTTP_SEE_OTHER = 303
        actual const val HTTP_NOT_MODIFIED = 304
        actual const val HTTP_USE_PROXY = 305
        actual const val HTTP_BAD_REQUEST = 400
        actual const val HTTP_UNAUTHORIZED = 401
        actual const val HTTP_PAYMENT_REQUIRED = 402
        actual const val HTTP_FORBIDDEN = 403
        actual const val HTTP_NOT_FOUND = 404
        actual const val HTTP_BAD_METHOD = 405
        actual const val HTTP_NOT_ACCEPTABLE = 406
        actual const val HTTP_PROXY_AUTH = 407
        actual const val HTTP_CLIENT_TIMEOUT = 408
        actual const val HTTP_CONFLICT = 409
        actual const val HTTP_GONE = 410
        actual const val HTTP_LENGTH_REQUIRED = 411
        actual const val HTTP_PRECON_FAILED = 412
        actual const val HTTP_ENTITY_TOO_LARGE = 413
        actual const val HTTP_REQ_TOO_LONG = 414
        actual const val HTTP_UNSUPPORTED_TYPE = 415
        actual const val HTTP_SERVER_ERROR = 500
        actual const val HTTP_INTERNAL_ERROR = 500
        actual const val HTTP_NOT_IMPLEMENTED = 501
        actual const val HTTP_BAD_GATEWAY = 502
        actual const val HTTP_UNAVAILABLE = 503
        actual const val HTTP_GATEWAY_TIMEOUT = 504
        actual const val HTTP_VERSION = 505
        const val HTTP_PERM_REDIRECT = 308
        const val HTTP_TEMP_REDIRECT = 307
    }
}

internal open class SocketHttpURLConnection(url: URL) : HttpURLConnection(url) {
    protected var socket: Socket? = null
    private val requestBodyBuffer = ByteArrayOutputStream()
    private var executed = false
    private val responseHeaders = LinkedHashMap<String?, MutableList<String>>()
    private val headerOrder = mutableListOf<Pair<String?, String>>()
    private var responseBodyBytes: ByteArray = ByteArray(0)
    private var responseBodyStream: InputStream? = null
    private var errorBodyStream: InputStream? = null

    protected open fun openTransportSocket(host: String, port: Int): Socket {
        val s = Socket()
        if (connectTimeout > 0) {
            s.connect(InetSocketAddress(host, port), connectTimeout)
        } else {
            s.connect(InetSocketAddress(host, port))
        }
        if (readTimeout > 0) {
            s.setSoTimeout(readTimeout)
        }
        return s
    }

    override fun connect() {
        if (connected) return
        val u = url ?: throw MalformedURLException("null URL")
        val host = u.getHost() ?: throw MalformedURLException("Missing host in URL: $u")
        val defaultPort = if (u.getProtocol()?.lowercase() == "https") 443 else 80
        val port = if (u.getPort() > 0) u.getPort() else defaultPort
        socket = openTransportSocket(host, port)
        connected = true
    }

    override fun disconnect() {
        connected = false
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
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
        val s = socket ?: throw IOException("Socket not connected")
        val u = url!!
        val host = u.getHost() ?: "localhost"
        val port = u.getPort()
        val defaultPort = if (u.getProtocol()?.lowercase() == "https") 443 else 80
        val hostHeader = if (port > 0 && port != defaultPort) "$host:$port" else host
        val pathAndQuery = u.getFile().ifEmpty { "/" }
        val bodyBytes = requestBodyBuffer.toByteArray()

        val req = StringBuilder()
        req.append(method).append(' ').append(pathAndQuery).append(" HTTP/1.1\r\n")
        if (getRequestProperty("Host") == null) {
            req.append("Host: ").append(hostHeader).append("\r\n")
        }
        if (getRequestProperty("Connection") == null) {
            req.append("Connection: close\r\n")
        }
        if (bodyBytes.isNotEmpty() && getRequestProperty("Content-Length") == null) {
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
        val statusLine = readHttpLine(inp) ?: throw IOException("Unexpected EOF reading HTTP status line")
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
