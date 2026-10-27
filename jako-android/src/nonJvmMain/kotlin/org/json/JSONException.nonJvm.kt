package org.json

import kotlin.math.floor

actual open class JSONException : Exception {
    actual constructor(s: String?) : super(s)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
