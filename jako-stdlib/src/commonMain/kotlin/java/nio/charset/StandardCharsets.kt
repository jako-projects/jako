@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

expect class StandardCharsets private constructor() {
    companion object {
        val US_ASCII: Charset
        val ISO_8859_1: Charset
        val UTF_8: Charset
        val UTF_16BE: Charset
        val UTF_16LE: Charset
        val UTF_16: Charset
    }
}
