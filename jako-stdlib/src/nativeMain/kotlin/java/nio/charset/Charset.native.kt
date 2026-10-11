package java.nio.charset

import java.nio.ByteBuffer
import java.nio.CharBuffer

actual abstract class Charset actual protected constructor(
    private val nameString: String,
    private val aliases: Array<String>
) : Comparable<Charset> {
    actual fun name(): String = nameString
    actual fun displayName(): String = nameString
    actual fun isRegistered(): Boolean = true

    actual fun newEncoder(): CharsetEncoder = nativeNewEncoder(this)
    actual fun newDecoder(): CharsetDecoder = nativeNewDecoder(this)

    actual fun encode(cb: CharBuffer): ByteBuffer = newEncoder().encode(cb)
    actual fun encode(str: String): ByteBuffer = encode(CharBuffer.wrap(str))
    actual fun decode(bb: ByteBuffer): CharBuffer = newDecoder().decode(bb)

    actual override fun compareTo(other: Charset): Int = nameString.compareTo(other.nameString, ignoreCase = true)

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Charset) return false
        return nameString.equals(other.nameString, ignoreCase = true)
    }

    actual override fun hashCode(): Int = nameString.lowercase().hashCode()
    actual override fun toString(): String = nameString

    actual companion object {
        private fun checkCharsetName(name: String) {
            if (name.isEmpty()) {
                throw IllegalCharsetNameException(name)
            }
            val first = name[0]
            if (!(first in 'a'..'z' || first in 'A'..'Z' || first in '0'..'9')) {
                throw IllegalCharsetNameException(name)
            }
            for (i in 1 until name.length) {
                val c = name[i]
                if (!(c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '.' || c == ':' || c == '_')) {
                    throw IllegalCharsetNameException(name)
                }
            }
        }

        actual fun isSupported(charsetName: String): Boolean {
            checkCharsetName(charsetName)
            return nativeCharsetKind(charsetName) != NativeCharsetKind.UNKNOWN
        }

        actual fun forName(charsetName: String): Charset {
            checkCharsetName(charsetName)
            val kind = nativeCharsetKind(charsetName)
            if (kind == NativeCharsetKind.UNKNOWN) {
                throw UnsupportedCharsetException(charsetName)
            }
            val canonicalName = when (kind) {
                NativeCharsetKind.UTF_8 -> "UTF-8"
                NativeCharsetKind.US_ASCII -> "US-ASCII"
                NativeCharsetKind.ISO_8859_1 -> "ISO-8859-1"
                NativeCharsetKind.UTF_16 -> "UTF-16"
                NativeCharsetKind.UTF_16BE -> "UTF-16BE"
                NativeCharsetKind.UTF_16LE -> "UTF-16LE"
                NativeCharsetKind.UTF_32 -> "UTF-32"
                NativeCharsetKind.UTF_32BE -> "UTF-32BE"
                NativeCharsetKind.UTF_32LE -> "UTF-32LE"
                NativeCharsetKind.UNKNOWN -> charsetName
            }
            return SimpleCharset(canonicalName)
        }

        actual fun defaultCharset(): Charset {
            return SimpleCharset("UTF-8")
        }
    }

    private class SimpleCharset(name: String) : Charset(name, emptyArray())
}
