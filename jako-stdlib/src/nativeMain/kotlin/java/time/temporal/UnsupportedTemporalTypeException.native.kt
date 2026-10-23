package java.time.temporal

import java.time.DateTimeException
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual open class UnsupportedTemporalTypeException : DateTimeException {
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
}
