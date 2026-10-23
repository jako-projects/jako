@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.IOException

expect open class HttpRetryException : IOException {
    constructor(detail: String?, code: Int)
    constructor(detail: String?, code: Int, location: String?)

    open fun responseCode(): Int
    open fun getReason(): String?
    open val reason: String?
    open fun getLocation(): String?
    open val location: String?
}
