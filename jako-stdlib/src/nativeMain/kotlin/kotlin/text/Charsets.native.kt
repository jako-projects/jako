package kotlin.text

import java.nio.charset.Charset

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

/**
 * Encodes this String to a ByteArray using the given [charset].
 * On JVM this is available as a stdlib function; on Native we provide a polyfill.
 *
 * Note: Currently only UTF-8 encoding is fully supported. Other charsets fall back to UTF-8.
 * This is sufficient for ASCII-compatible charsets (US-ASCII, ISO-8859-1) within the ASCII range.
 */
actual fun String.toByteArray(charset: Charset): ByteArray = encodeToByteArray(charset)

private fun String.encodeToByteArray(charset: Charset): ByteArray {
    val charsetName = charset.name().uppercase().replace("_", "-")
    if (charsetName == "UTF-8" || charsetName == "UTF8") {
        return this.encodeToByteArray()
    }
    if (charsetName == "ISO-8859-1" || charsetName == "LATIN1" ||
        charsetName == "US-ASCII" || charsetName == "ASCII") {
        return ByteArray(this.length) { i -> this[i].code.toByte() }
    }
    if (charsetName == "UTF-16BE" || charsetName == "UTF-16") {
        val bytes = ByteArray(this.length * 2)
        for (i in indices) {
            val code = this[i].code
            bytes[i * 2] = (code ushr 8).toByte()
            bytes[i * 2 + 1] = (code and 0xFF).toByte()
        }
        return bytes
    }
    if (charsetName == "UTF-16LE") {
        val bytes = ByteArray(this.length * 2)
        for (i in indices) {
            val code = this[i].code
            bytes[i * 2] = (code and 0xFF).toByte()
            bytes[i * 2 + 1] = (code ushr 8).toByte()
        }
        return bytes
    }
    // Fallback: UTF-8 encoding
    return this.encodeToByteArray()
}
