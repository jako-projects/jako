@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.InputStream
import java.io.OutputStream

expect abstract class URLConnection {
    protected constructor(url: URL?)
    abstract fun connect()
    open fun getURL(): URL?
    open val url: URL?
    open fun setConnectTimeout(timeout: Int)
    open fun getConnectTimeout(): Int
    open var connectTimeout: Int
    open fun setReadTimeout(timeout: Int)
    open fun getReadTimeout(): Int
    open var readTimeout: Int
    open fun setDoInput(doinput: Boolean)
    open fun getDoInput(): Boolean
    open var doInput: Boolean
    open fun setDoOutput(dooutput: Boolean)
    open fun getDoOutput(): Boolean
    open var doOutput: Boolean
    open fun setUseCaches(usecaches: Boolean)
    open fun getUseCaches(): Boolean
    open var useCaches: Boolean
    open fun setRequestProperty(key: String, value: String?)
    open fun addRequestProperty(key: String, value: String?)
    open fun getRequestProperty(key: String): String?
    open fun getRequestProperties(): Map<String, List<String>>
    open fun getHeaderField(name: String): String?
    open fun getHeaderField(n: Int): String?
    open fun getHeaderFieldKey(n: Int): String?
    open fun getHeaderFields(): Map<String?, List<String>>
    open fun getContentLength(): Int
    open val contentLength: Int
    open fun getContentLengthLong(): Long
    open val contentLengthLong: Long
    open fun getContentType(): String?
    open val contentType: String?
    open fun getContentEncoding(): String?
    open val contentEncoding: String?
    open fun getInputStream(): InputStream
    open val inputStream: InputStream
    open fun getOutputStream(): OutputStream
    open val outputStream: OutputStream
}
