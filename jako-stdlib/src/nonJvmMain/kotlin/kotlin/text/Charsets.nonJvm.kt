package kotlin.text

import java.nio.charset.Charset
import kotlin.jvm.JvmField

/**
 * [Nullability Decision] All Charset references are non-null, matching JVM kotlin.text.Charsets.
 *
 * Polyfill for kotlin.text.Charsets which is available on JVM but not on Kotlin Native.
 */
actual object Charsets {
    @JvmField actual val UTF_8: Charset = Charset.forName("UTF-8")
    @JvmField actual val ISO_8859_1: Charset = Charset.forName("ISO-8859-1")
    @JvmField actual val US_ASCII: Charset = Charset.forName("US-ASCII")
    @JvmField actual val UTF_16: Charset = Charset.forName("UTF-16")
    @JvmField actual val UTF_16BE: Charset = Charset.forName("UTF-16BE")
    @JvmField actual val UTF_16LE: Charset = Charset.forName("UTF-16LE")
    @JvmField actual val UTF_32BE: Charset = Charset.forName("UTF-32BE")
    @JvmField actual val UTF_32LE: Charset = Charset.forName("UTF-32LE")
}
