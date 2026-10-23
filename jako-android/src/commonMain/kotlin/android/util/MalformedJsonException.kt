@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.io.IOException

expect open class MalformedJsonException : IOException {
    constructor(message: String?)
}
