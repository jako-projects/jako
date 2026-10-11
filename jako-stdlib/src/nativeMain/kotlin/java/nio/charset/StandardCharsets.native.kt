package java.nio.charset

actual class StandardCharsets actual private constructor() {
    actual companion object {
        actual val US_ASCII: Charset = Charset.forName("US-ASCII")
        actual val ISO_8859_1: Charset = Charset.forName("ISO-8859-1")
        actual val UTF_8: Charset = Charset.forName("UTF-8")
        actual val UTF_16BE: Charset = Charset.forName("UTF-16BE")
        actual val UTF_16LE: Charset = Charset.forName("UTF-16LE")
        actual val UTF_16: Charset = Charset.forName("UTF-16")
    }
}
