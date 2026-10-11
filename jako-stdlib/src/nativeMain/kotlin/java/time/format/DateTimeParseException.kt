package java.time.format

import java.time.DateTimeException

actual open class DateTimeParseException : DateTimeException {
    actual val parsedString: String
    actual val errorIndex: Int

    actual constructor(message: String?, parsedData: CharSequence, errorIndex: Int) :
        this(message, parsedData, errorIndex, null)

    actual constructor(message: String?, parsedData: CharSequence, errorIndex: Int, cause: Throwable?) :
        super(message, cause) {
        // [Nullability Decision] parsedData is non-null; stored as String like JDK 8 getParsedString()
        this.parsedString = parsedData.toString()
        this.errorIndex = errorIndex
    }

    actual fun getParsedString(): String = parsedString
    actual fun getErrorIndex(): Int = errorIndex
}
