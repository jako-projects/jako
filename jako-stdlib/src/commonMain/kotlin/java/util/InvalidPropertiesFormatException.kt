@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class InvalidPropertiesFormatException : java.io.IOException {
    constructor(cause: Throwable?)
    constructor(message: String?)
}
