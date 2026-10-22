package java.net

import java.io.InputStream
import java.io.OutputStream
import java.util.Collections

actual abstract class URLConnection protected actual constructor(actual open val url: URL?) {
    protected var connected: Boolean = false
    private var _connectTimeout: Int = 0
    private var _readTimeout: Int = 0
    private var _doInput: Boolean = true
    private var _doOutput: Boolean = false
    private var _useCaches: Boolean = true
    protected val requestPropertiesMap = LinkedHashMap<String, MutableList<String>>()

    actual abstract fun connect()

    actual open fun getURL(): URL? = url

    actual open fun setConnectTimeout(timeout: Int) {
        require(timeout >= 0) { "timeout can not be negative" }
        _connectTimeout = timeout
    }

    actual open fun getConnectTimeout(): Int = _connectTimeout

    actual open var connectTimeout: Int
        get() = getConnectTimeout()
        set(value) = setConnectTimeout(value)

    actual open fun setReadTimeout(timeout: Int) {
        require(timeout >= 0) { "timeout can not be negative" }
        _readTimeout = timeout
    }

    actual open fun getReadTimeout(): Int = _readTimeout

    actual open var readTimeout: Int
        get() = getReadTimeout()
        set(value) = setReadTimeout(value)

    actual open fun setDoInput(doinput: Boolean) {
        _doInput = doinput
    }

    actual open fun getDoInput(): Boolean = _doInput

    actual open var doInput: Boolean
        get() = getDoInput()
        set(value) = setDoInput(value)

    actual open fun setDoOutput(dooutput: Boolean) {
        _doOutput = dooutput
    }

    actual open fun getDoOutput(): Boolean = _doOutput

    actual open var doOutput: Boolean
        get() = getDoOutput()
        set(value) = setDoOutput(value)

    actual open fun setUseCaches(usecaches: Boolean) {
        _useCaches = usecaches
    }

    actual open fun getUseCaches(): Boolean = _useCaches

    actual open var useCaches: Boolean
        get() = getUseCaches()
        set(value) = setUseCaches(value)

    actual open fun setRequestProperty(key: String, value: String?) {
        if (value == null) {
            requestPropertiesMap.remove(key)
        } else {
            requestPropertiesMap[key] = mutableListOf(value)
        }
    }

    actual open fun addRequestProperty(key: String, value: String?) {
        if (value != null) {
            requestPropertiesMap.getOrPut(key) { mutableListOf() }.add(value)
        }
    }

    actual open fun getRequestProperty(key: String): String? {
        for ((k, v) in requestPropertiesMap) {
            if (k.equals(key, ignoreCase = true)) {
                return v.lastOrNull()
            }
        }
        return null
    }

    actual open fun getRequestProperties(): Map<String, List<String>> {
        val copy = LinkedHashMap<String, List<String>>()
        for ((k, v) in requestPropertiesMap) {
            copy[k] = v.toList()
        }
        return Collections.unmodifiableMap(copy)
    }

    actual open fun getHeaderField(name: String): String? = null
    actual open fun getHeaderField(n: Int): String? = null
    actual open fun getHeaderFieldKey(n: Int): String? = null
    actual open fun getHeaderFields(): Map<String?, List<String>> = emptyMap()

    actual open fun getContentLength(): Int {
        val l = getContentLengthLong()
        return if (l > Int.MAX_VALUE) -1 else l.toInt()
    }

    actual open val contentLength: Int get() = getContentLength()

    actual open fun getContentLengthLong(): Long {
        return getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
    }

    actual open val contentLengthLong: Long get() = getContentLengthLong()

    actual open fun getContentType(): String? = getHeaderField("Content-Type")
    actual open val contentType: String? get() = getContentType()

    actual open fun getContentEncoding(): String? = getHeaderField("Content-Encoding")
    actual open val contentEncoding: String? get() = getContentEncoding()

    actual open fun getInputStream(): InputStream {
        throw UnknownServiceException("protocol doesn't support input")
    }

    actual open val inputStream: InputStream get() = getInputStream()

    actual open fun getOutputStream(): OutputStream {
        throw UnknownServiceException("protocol doesn't support output")
    }

    actual open val outputStream: OutputStream get() = getOutputStream()
}
