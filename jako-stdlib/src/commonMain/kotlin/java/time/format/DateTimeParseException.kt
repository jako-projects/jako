@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.format

import java.time.DateTimeException

expect open class DateTimeParseException : DateTimeException {
    // [Nullability Decision] message may be null; parsedData is non-null, matching JDK 8
    constructor(message: String?, parsedData: CharSequence, errorIndex: Int)
    constructor(message: String?, parsedData: CharSequence, errorIndex: Int, cause: Throwable?)

    val parsedString: String
    val errorIndex: Int
    fun getParsedString(): String
    fun getErrorIndex(): Int
}
