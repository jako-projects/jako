@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

expect open class DateTimeException : RuntimeException {
    // [Nullability Decision] message may be null, matching JDK 8 RuntimeException
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
}
