@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.InputStream

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking HTTP connections over Socket require Node.js worker_threads and net module")
expect abstract class HttpURLConnection : URLConnection {
    protected constructor(url: URL?)
    abstract fun disconnect()
    abstract fun usingProxy(): Boolean
    open fun setRequestMethod(method: String)
    open fun getRequestMethod(): String
    open var requestMethod: String
    open fun setInstanceFollowRedirects(followRedirects: Boolean)
    open fun getInstanceFollowRedirects(): Boolean
    open var instanceFollowRedirects: Boolean
    open fun getErrorStream(): InputStream?
    open val errorStream: InputStream?
    open fun getResponseCode(): Int
    open fun getResponseMessage(): String?

    open val responseCode: Int
    open val responseMessage: String?

    companion object {
        val HTTP_OK: Int
        val HTTP_CREATED: Int
        val HTTP_ACCEPTED: Int
        val HTTP_NOT_AUTHORITATIVE: Int
        val HTTP_NO_CONTENT: Int
        val HTTP_RESET: Int
        val HTTP_PARTIAL: Int
        val HTTP_MULT_CHOICE: Int
        val HTTP_MOVED_PERM: Int
        val HTTP_MOVED_TEMP: Int
        val HTTP_SEE_OTHER: Int
        val HTTP_NOT_MODIFIED: Int
        val HTTP_USE_PROXY: Int
        val HTTP_BAD_REQUEST: Int
        val HTTP_UNAUTHORIZED: Int
        val HTTP_PAYMENT_REQUIRED: Int
        val HTTP_FORBIDDEN: Int
        val HTTP_NOT_FOUND: Int
        val HTTP_BAD_METHOD: Int
        val HTTP_NOT_ACCEPTABLE: Int
        val HTTP_PROXY_AUTH: Int
        val HTTP_CLIENT_TIMEOUT: Int
        val HTTP_CONFLICT: Int
        val HTTP_GONE: Int
        val HTTP_LENGTH_REQUIRED: Int
        val HTTP_PRECON_FAILED: Int
        val HTTP_ENTITY_TOO_LARGE: Int
        val HTTP_REQ_TOO_LONG: Int
        val HTTP_UNSUPPORTED_TYPE: Int
        val HTTP_SERVER_ERROR: Int
        val HTTP_INTERNAL_ERROR: Int
        val HTTP_NOT_IMPLEMENTED: Int
        val HTTP_BAD_GATEWAY: Int
        val HTTP_UNAVAILABLE: Int
        val HTTP_GATEWAY_TIMEOUT: Int
        val HTTP_VERSION: Int
    }
}
