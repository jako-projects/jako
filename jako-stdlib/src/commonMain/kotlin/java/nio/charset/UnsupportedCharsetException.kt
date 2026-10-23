@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

expect open class UnsupportedCharsetException : IllegalArgumentException {
    /** [Nullability Decision] Java UnsupportedCharsetException(String) charsetName is non-null. */
    constructor(charsetName: String)
    fun getCharsetName(): String
    val charsetName: String
}
