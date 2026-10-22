@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package kotlin.text

import java.nio.charset.Charset

expect object Charsets {
    val UTF_8: Charset
    val ISO_8859_1: Charset
    val US_ASCII: Charset
    val UTF_16: Charset
    val UTF_16BE: Charset
    val UTF_16LE: Charset
    val UTF_32BE: Charset
    val UTF_32LE: Charset
}
