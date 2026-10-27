package kotlin.text

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

public actual fun String(codePoints: IntArray, offset: Int, count: Int): String {
    if (offset < 0 || count < 0 || offset > codePoints.size - count) {
        throw IndexOutOfBoundsException("offset=$offset, count=$count, size=${codePoints.size}")
    }
    val sb = StringBuilder(count)
    for (i in offset until offset + count) {
        val cp = codePoints[i]
        if (cp < 0 || cp > 0x10FFFF) {
            throw IllegalArgumentException("Invalid code point: $cp")
        }
        if (cp <= 0xFFFF) {
            sb.append(cp.toChar())
        } else {
            val hi = ((cp - 0x10000) ushr 10) + 0xD800
            val lo = ((cp - 0x10000) and 0x3FF) + 0xDC00
            sb.append(hi.toChar())
            sb.append(lo.toChar())
        }
    }
    return sb.toString()
}

public actual fun String(bytes: ByteArray, offset: Int, length: Int, charset: Charset): String {
    if (offset < 0 || length < 0 || offset > bytes.size - length) {
        throw IndexOutOfBoundsException("offset=$offset, length=$length, size=${bytes.size}")
    }
    val cs = charset.name().uppercase().replace("_", "-")
    if (cs == "UTF-8" || cs == "UTF8" || cs == "US-ASCII" || cs == "ASCII") {
        return bytes.decodeToString(offset, offset + length)
    }
    val bb = ByteBuffer.wrap(bytes, offset, length)
    val decoder = charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
    val cb = decoder.decode(bb)
    return cb.toString()
}

public actual fun String(bytes: ByteArray, charset: Charset): String =
    String(bytes, 0, bytes.size, charset)

public actual fun String.toByteArray(charset: Charset): ByteArray = encodeToByteArray(charset)

private fun String.encodeToByteArray(charset: Charset): ByteArray {
    val charsetName = charset.name().uppercase().replace("_", "-")
    if (charsetName == "UTF-8" || charsetName == "UTF8") {
        return this.encodeToByteArray()
    }
    if (charsetName == "ISO-8859-1" || charsetName == "LATIN1" ||
        charsetName == "US-ASCII" || charsetName == "ASCII"
    ) {
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
    return this.encodeToByteArray()
}
