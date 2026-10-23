@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.time.DateTimeException

/**
 * Indicates that a ChronoField or ChronoUnit is not supported for a Temporal class.
 */
expect open class UnsupportedTemporalTypeException : DateTimeException {
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
}
