package java.nio.charset

actual open class UnsupportedCharsetException actual constructor(
    charsetName: String
) : IllegalArgumentException(charsetName) {
    private val name: String = charsetName
    actual fun getCharsetName(): String = name
    actual val charsetName: String get() = name
}
