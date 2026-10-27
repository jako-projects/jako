package java.net

import java.io.IOException

actual open class HttpRetryException : IOException {
    private val responseCodeVal: Int
    private val locationVal: String?

    actual constructor(detail: String?, code: Int) : this(detail, code, null)

    actual constructor(detail: String?, code: Int, location: String?) : super(detail) {
        this.responseCodeVal = code
        this.locationVal = location
    }

    actual open fun responseCode(): Int = responseCodeVal

    actual open fun getReason(): String? = message

    actual open val reason: String?
        get() = message

    actual open fun getLocation(): String? = locationVal

    actual open val location: String?
        get() = locationVal
}
