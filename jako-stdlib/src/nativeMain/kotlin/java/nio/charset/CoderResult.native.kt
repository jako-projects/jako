package java.nio.charset

import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException

internal const val CR_UNDERFLOW = 0

internal const val CR_OVERFLOW = 1

internal const val CR_MALFORMED = 2

internal const val CR_UNMAPPABLE = 3

actual open class CoderResult {
    private val kind: Int
    private val len: Int

    constructor() : this(CR_UNDERFLOW, 0)

    private constructor(kind: Int, len: Int) {
        this.kind = kind
        this.len = len
    }

    actual fun isUnderflow(): Boolean = kind == CR_UNDERFLOW
    actual fun isOverflow(): Boolean = kind == CR_OVERFLOW
    actual fun isError(): Boolean = kind == CR_MALFORMED || kind == CR_UNMAPPABLE
    actual fun isMalformed(): Boolean = kind == CR_MALFORMED
    actual fun isUnmappable(): Boolean = kind == CR_UNMAPPABLE

    actual fun length(): Int {
        if (!isError()) throw UnsupportedOperationException()
        return len
    }

    actual fun throwException() {
        when (kind) {
            CR_UNDERFLOW -> throw BufferUnderflowException()
            CR_OVERFLOW -> throw BufferOverflowException()
            CR_MALFORMED -> throw MalformedInputException(len)
            CR_UNMAPPABLE -> throw UnmappableCharacterException(len)
        }
    }

    actual val isUnderflow: Boolean get() = isUnderflow()
    actual val isOverflow: Boolean get() = isOverflow()
    actual val isError: Boolean get() = isError()
    actual val isMalformed: Boolean get() = isMalformed()
    actual val isUnmappable: Boolean get() = isUnmappable()

    actual companion object {
        actual val UNDERFLOW: CoderResult = CoderResult(CR_UNDERFLOW, 0)
        actual val OVERFLOW: CoderResult = CoderResult(CR_OVERFLOW, 0)

        actual fun malformedForLength(length: Int): CoderResult {
            if (length <= 0) throw IllegalArgumentException("Non-positive length")
            return CoderResult(CR_MALFORMED, length)
        }

        actual fun unmappableForLength(length: Int): CoderResult {
            if (length <= 0) throw IllegalArgumentException("Non-positive length")
            return CoderResult(CR_UNMAPPABLE, length)
        }
    }
}

internal enum class NativeCharsetKind {
    UTF_8, US_ASCII, ISO_8859_1, UTF_16, UTF_16BE, UTF_16LE, UTF_32, UTF_32BE, UTF_32LE, UNKNOWN
}

internal fun nativeCharsetKind(name: String): NativeCharsetKind {
    val n = name.uppercase().replace('_', '-')
    return when (n) {
        "UTF-8", "UTF8" -> NativeCharsetKind.UTF_8
        "US-ASCII", "ASCII", "ANSI-X3.4-1968", "ISO646-US" -> NativeCharsetKind.US_ASCII
        "ISO-8859-1", "ISO8859-1", "LATIN1", "L1", "ISO-LATIN-1" -> NativeCharsetKind.ISO_8859_1
        "UTF-16" -> NativeCharsetKind.UTF_16
        "UTF-16BE" -> NativeCharsetKind.UTF_16BE
        "UTF-16LE" -> NativeCharsetKind.UTF_16LE
        "UTF-32", "UTF32" -> NativeCharsetKind.UTF_32
        "UTF-32BE" -> NativeCharsetKind.UTF_32BE
        "UTF-32LE" -> NativeCharsetKind.UTF_32LE
        else -> NativeCharsetKind.UNKNOWN
    }
}

internal fun nativeNewEncoder(cs: Charset): CharsetEncoder {
    val kind = nativeCharsetKind(cs.name())
    if (kind == NativeCharsetKind.UNKNOWN) throw UnsupportedCharsetException(cs.name())
    val avgMax = when (kind) {
        NativeCharsetKind.UTF_8 -> 1.1f to 3.0f
        NativeCharsetKind.US_ASCII, NativeCharsetKind.ISO_8859_1 -> 1.0f to 1.0f
        NativeCharsetKind.UTF_16 -> 2.0f to 4.0f
        NativeCharsetKind.UTF_16BE, NativeCharsetKind.UTF_16LE -> 2.0f to 2.0f
        NativeCharsetKind.UTF_32, NativeCharsetKind.UTF_32BE, NativeCharsetKind.UTF_32LE -> 4.0f to 4.0f
        NativeCharsetKind.UNKNOWN -> 1.0f to 1.0f
    }
    return NativeCharsetEncoder(cs, kind, avgMax.first, avgMax.second)
}

internal fun nativeNewDecoder(cs: Charset): CharsetDecoder {
    val kind = nativeCharsetKind(cs.name())
    if (kind == NativeCharsetKind.UNKNOWN) throw UnsupportedCharsetException(cs.name())
    val avgMax = when (kind) {
        NativeCharsetKind.UTF_16, NativeCharsetKind.UTF_16BE, NativeCharsetKind.UTF_16LE -> 0.5f to 1.0f
        NativeCharsetKind.UTF_32, NativeCharsetKind.UTF_32BE, NativeCharsetKind.UTF_32LE -> 0.25f to 1.0f
        else -> 1.0f to 1.0f
    }
    return NativeCharsetDecoder(cs, kind, avgMax.first, avgMax.second)
}
