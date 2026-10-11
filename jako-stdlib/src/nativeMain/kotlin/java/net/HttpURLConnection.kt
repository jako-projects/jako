package java.net

import java.io.InputStream

actual abstract class HttpURLConnection protected actual constructor(url: URL?) : URLConnection(url) {
    actual abstract fun disconnect()
    actual abstract fun usingProxy(): Boolean
    actual abstract fun getResponseCode(): Int
    actual abstract fun getResponseMessage(): String?

    actual open val responseCode: Int get() = getResponseCode()
    actual open val responseMessage: String? get() = getResponseMessage()

    open fun getErrorStream(): InputStream? = null

    actual companion object {
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
