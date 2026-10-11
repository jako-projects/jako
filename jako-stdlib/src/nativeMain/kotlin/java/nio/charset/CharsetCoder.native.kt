package java.nio.charset

import java.io.IOException
import java.nio.BufferOverflowException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer

actual open class CharacterCodingException actual constructor() : IOException()

actual open class MalformedInputException actual constructor(
    inputLength: Int
) : CharacterCodingException() {
    private val length: Int = inputLength
    actual fun getInputLength(): Int = length
    actual val inputLength: Int get() = length
    override val message: String? get() = "Input length = $length"
}

actual open class UnmappableCharacterException actual constructor(
    inputLength: Int
) : CharacterCodingException() {
    private val length: Int = inputLength
    actual fun getInputLength(): Int = length
    actual val inputLength: Int get() = length
    override val message: String? get() = "Input length = $length"
}

actual open class IllegalCharsetNameException actual constructor(
    charsetName: String
) : IllegalArgumentException(charsetName) {
    private val name: String = charsetName
    actual fun getCharsetName(): String = name
    actual val charsetName: String get() = name
}

actual open class UnsupportedCharsetException actual constructor(
    charsetName: String
) : IllegalArgumentException(charsetName) {
    private val name: String = charsetName
    actual fun getCharsetName(): String = name
    actual val charsetName: String get() = name
}

actual class CodingErrorAction {
    private val nameStr: String

    constructor() : this("REPORT")

    private constructor(name: String) {
        this.nameStr = name
    }

    actual override fun toString(): String = nameStr

    actual companion object {
        actual val IGNORE: CodingErrorAction = CodingErrorAction("IGNORE")
        actual val REPLACE: CodingErrorAction = CodingErrorAction("REPLACE")
        actual val REPORT: CodingErrorAction = CodingErrorAction("REPORT")
    }
}

private const val CR_UNDERFLOW = 0
private const val CR_OVERFLOW = 1
private const val CR_MALFORMED = 2
private const val CR_UNMAPPABLE = 3

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

private const val ST_RESET = 0
private const val ST_CODING = 1
private const val ST_END = 2
private const val ST_FLUSHED = 3

actual abstract class CharsetEncoder {
    private val cs: Charset
    private val avg: Float
    private val max: Float
    private var repl: ByteArray
    private var malformedAction: CodingErrorAction = CodingErrorAction.REPORT
    private var unmappableAction: CodingErrorAction = CodingErrorAction.REPORT
    private var state: Int = ST_RESET

    protected actual constructor(cs: Charset, averageBytesPerChar: Float, maxBytesPerChar: Float) :
        this(cs, averageBytesPerChar, maxBytesPerChar, byteArrayOf(0x3f))

    protected actual constructor(
        cs: Charset,
        averageBytesPerChar: Float,
        maxBytesPerChar: Float,
        replacement: ByteArray
    ) {
        this.cs = cs
        this.avg = averageBytesPerChar
        this.max = maxBytesPerChar
        this.repl = replacement.copyOf()
    }

    actual fun charset(): Charset = cs
    actual fun replacement(): ByteArray = repl.copyOf()

    actual fun replaceWith(newReplacement: ByteArray): CharsetEncoder {
        if (newReplacement.isEmpty()) throw IllegalArgumentException("Empty replacement")
        repl = newReplacement.copyOf()
        return this
    }

    actual fun malformedInputAction(): CodingErrorAction = malformedAction
    actual fun unmappableCharacterAction(): CodingErrorAction = unmappableAction

    actual fun onMalformedInput(newAction: CodingErrorAction): CharsetEncoder {
        malformedAction = newAction
        return this
    }

    actual fun onUnmappableCharacter(newAction: CodingErrorAction): CharsetEncoder {
        unmappableAction = newAction
        return this
    }

    actual fun averageBytesPerChar(): Float = avg
    actual fun maxBytesPerChar(): Float = max

    actual fun reset(): CharsetEncoder {
        state = ST_RESET
        implReset()
        return this
    }

    actual fun encode(`in`: CharBuffer, out: ByteBuffer, endOfInput: Boolean): CoderResult {
        if (state == ST_FLUSHED || (state == ST_END && !endOfInput)) {
            throw IllegalStateException("Encoder in wrong state")
        }
        if (state == ST_RESET) state = ST_CODING
        if (endOfInput) state = ST_END
        while (true) {
            val cr = encodeLoop(`in`, out)
            if (cr.isOverflow || cr.isUnderflow) {
                if (cr.isUnderflow && endOfInput && `in`.hasRemaining()) {
                    return handleError(CoderResult.malformedForLength(`in`.remaining()), `in`, out, malformedAction)
                        ?: continue
                }
                return cr
            }
            val action = if (cr.isUnmappable) unmappableAction else malformedAction
            handleError(cr, `in`, out, action) ?: continue
            return cr
        }
    }

    actual fun encode(`in`: CharBuffer): ByteBuffer {
        var n = (`in`.remaining() * avg).toInt()
        var out = ByteBuffer.allocate(n)
        if (n == 0 && `in`.remaining() == 0) return out
        reset()
        while (true) {
            var cr = if (`in`.hasRemaining()) encode(`in`, out, true) else CoderResult.UNDERFLOW
            if (cr.isUnderflow) cr = flush(out)
            if (cr.isUnderflow) break
            if (cr.isOverflow) {
                n = 2 * n + 1
                val o = ByteBuffer.allocate(n)
                out.flip()
                o.put(out)
                out = o
                continue
            }
            cr.throwException()
        }
        out.flip()
        return out
    }

    actual fun flush(out: ByteBuffer): CoderResult {
        if (state == ST_FLUSHED) return CoderResult.UNDERFLOW
        if (state != ST_END && state != ST_RESET) {
            throw IllegalStateException("Encoder not at end of input")
        }
        val cr = implFlush(out)
        if (cr.isUnderflow) state = ST_FLUSHED
        return cr
    }

    actual val charset: Charset get() = charset()
    actual val replacement: ByteArray get() = replacement()
    actual val malformedInputAction: CodingErrorAction get() = malformedInputAction()
    actual val unmappableCharacterAction: CodingErrorAction get() = unmappableCharacterAction()
    actual val averageBytesPerChar: Float get() = averageBytesPerChar()
    actual val maxBytesPerChar: Float get() = maxBytesPerChar()

    protected abstract fun encodeLoop(`in`: CharBuffer, out: ByteBuffer): CoderResult

    protected open fun implReset() {}

    protected open fun implFlush(out: ByteBuffer): CoderResult = CoderResult.UNDERFLOW

    private fun handleError(
        cr: CoderResult,
        `in`: CharBuffer,
        out: ByteBuffer,
        action: CodingErrorAction
    ): CoderResult? {
        when (action) {
            CodingErrorAction.REPORT -> return cr
            CodingErrorAction.IGNORE -> {
                skipInput(`in`, cr.length())
                return null
            }
            CodingErrorAction.REPLACE -> {
                if (out.remaining() < repl.size) return CoderResult.OVERFLOW
                out.put(repl)
                skipInput(`in`, cr.length())
                return null
            }
            else -> return cr
        }
    }

    private fun skipInput(`in`: CharBuffer, n: Int) {
        val p = `in`.position()
        `in`.position(p + n)
    }
}

actual abstract class CharsetDecoder {
    private val cs: Charset
    private val avg: Float
    private val max: Float
    private var repl: String = "\uFFFD"
    private var malformedAction: CodingErrorAction = CodingErrorAction.REPORT
    private var unmappableAction: CodingErrorAction = CodingErrorAction.REPORT
    private var state: Int = ST_RESET

    protected actual constructor(cs: Charset, averageCharsPerByte: Float, maxCharsPerByte: Float) {
        this.cs = cs
        this.avg = averageCharsPerByte
        this.max = maxCharsPerByte
    }

    actual fun charset(): Charset = cs
    actual fun replacement(): String = repl

    actual fun replaceWith(newReplacement: String): CharsetDecoder {
        if (newReplacement.isEmpty()) throw IllegalArgumentException("Empty replacement")
        repl = newReplacement
        return this
    }

    actual fun malformedInputAction(): CodingErrorAction = malformedAction
    actual fun unmappableCharacterAction(): CodingErrorAction = unmappableAction

    actual fun onMalformedInput(newAction: CodingErrorAction): CharsetDecoder {
        malformedAction = newAction
        return this
    }

    actual fun onUnmappableCharacter(newAction: CodingErrorAction): CharsetDecoder {
        unmappableAction = newAction
        return this
    }

    actual fun averageCharsPerByte(): Float = avg
    actual fun maxCharsPerByte(): Float = max

    actual fun reset(): CharsetDecoder {
        state = ST_RESET
        implReset()
        return this
    }

    actual fun decode(`in`: ByteBuffer, out: CharBuffer, endOfInput: Boolean): CoderResult {
        if (state == ST_FLUSHED || (state == ST_END && !endOfInput)) {
            throw IllegalStateException("Decoder in wrong state")
        }
        if (state == ST_RESET) state = ST_CODING
        if (endOfInput) state = ST_END
        while (true) {
            val cr = decodeLoop(`in`, out)
            if (cr.isOverflow || cr.isUnderflow) {
                if (cr.isUnderflow && endOfInput && `in`.hasRemaining()) {
                    return handleError(CoderResult.malformedForLength(`in`.remaining()), `in`, out, malformedAction)
                        ?: continue
                }
                return cr
            }
            val action = if (cr.isUnmappable) unmappableAction else malformedAction
            handleError(cr, `in`, out, action) ?: continue
            return cr
        }
    }

    actual fun decode(`in`: ByteBuffer): CharBuffer {
        var n = (`in`.remaining() * avg).toInt()
        var out = CharBuffer.allocate(if (n == 0) 1 else n)
        if (`in`.remaining() == 0) {
            out.flip()
            return out
        }
        reset()
        while (true) {
            var cr = if (`in`.hasRemaining()) decode(`in`, out, true) else CoderResult.UNDERFLOW
            if (cr.isUnderflow) cr = flush(out)
            if (cr.isUnderflow) break
            if (cr.isOverflow) {
                n = 2 * n + 1
                val o = CharBuffer.allocate(n)
                out.flip()
                o.put(out)
                out = o
                continue
            }
            cr.throwException()
        }
        out.flip()
        return out
    }

    actual fun flush(out: CharBuffer): CoderResult {
        if (state == ST_FLUSHED) return CoderResult.UNDERFLOW
        val cr = implFlush(out)
        if (cr.isUnderflow) state = ST_FLUSHED
        return cr
    }

    actual val charset: Charset get() = charset()
    actual val replacement: String get() = replacement()
    actual val malformedInputAction: CodingErrorAction get() = malformedInputAction()
    actual val unmappableCharacterAction: CodingErrorAction get() = unmappableCharacterAction()
    actual val averageCharsPerByte: Float get() = averageCharsPerByte()
    actual val maxCharsPerByte: Float get() = maxCharsPerByte()

    protected abstract fun decodeLoop(`in`: ByteBuffer, out: CharBuffer): CoderResult

    protected open fun implReset() {}

    protected open fun implFlush(out: CharBuffer): CoderResult = CoderResult.UNDERFLOW

    private fun handleError(
        cr: CoderResult,
        `in`: ByteBuffer,
        out: CharBuffer,
        action: CodingErrorAction
    ): CoderResult? {
        when (action) {
            CodingErrorAction.REPORT -> return cr
            CodingErrorAction.IGNORE -> {
                `in`.position(`in`.position() + cr.length())
                return null
            }
            CodingErrorAction.REPLACE -> {
                if (out.remaining() < repl.length) return CoderResult.OVERFLOW
                out.put(repl)
                `in`.position(`in`.position() + cr.length())
                return null
            }
            else -> return cr
        }
    }
}

internal class NativeCharsetEncoder(
    cs: Charset,
    private val kind: NativeCharsetKind,
    avg: Float,
    max: Float
) : CharsetEncoder(cs, avg, max) {

    private var bomWritten = false

    override fun implReset() {
        bomWritten = false
    }

    override fun encodeLoop(`in`: CharBuffer, out: ByteBuffer): CoderResult {
        if (kind == NativeCharsetKind.UTF_16 && !bomWritten) {
            if (out.remaining() < 2) return CoderResult.OVERFLOW
            out.put(0xFE.toByte())
            out.put(0xFF.toByte())
            bomWritten = true
        } else if (kind == NativeCharsetKind.UTF_32 && !bomWritten) {
            if (out.remaining() < 4) return CoderResult.OVERFLOW
            out.put(0x00.toByte())
            out.put(0x00.toByte())
            out.put(0xFE.toByte())
            out.put(0xFF.toByte())
            bomWritten = true
        }
        val order = when (kind) {
            NativeCharsetKind.UTF_16LE, NativeCharsetKind.UTF_32LE -> ByteOrder.LITTLE_ENDIAN
            else -> ByteOrder.BIG_ENDIAN
        }
        var ip = `in`.position()
        val ilim = `in`.limit()
        while (ip < ilim) {
            val c = `in`.get(ip)
            when (kind) {
                NativeCharsetKind.US_ASCII -> {
                    if (c.code > 0x7F) {
                        `in`.position(ip)
                        return CoderResult.unmappableForLength(1)
                    }
                    if (out.remaining() < 1) {
                        `in`.position(ip)
                        return CoderResult.OVERFLOW
                    }
                    out.put(c.code.toByte())
                    ip++
                }
                NativeCharsetKind.ISO_8859_1 -> {
                    if (c.code > 0xFF) {
                        `in`.position(ip)
                        return CoderResult.unmappableForLength(1)
                    }
                    if (out.remaining() < 1) {
                        `in`.position(ip)
                        return CoderResult.OVERFLOW
                    }
                    out.put(c.code.toByte())
                    ip++
                }
                NativeCharsetKind.UTF_8 -> {
                    val cr = encodeUtf8(`in`, out, ip)
                    if (cr != null) return cr
                    ip = `in`.position()
                    continue
                }
                NativeCharsetKind.UTF_16, NativeCharsetKind.UTF_16BE, NativeCharsetKind.UTF_16LE -> {
                    val cr = encodeUtf16(`in`, out, ip, order)
                    if (cr != null) return cr
                    ip = `in`.position()
                    continue
                }
                NativeCharsetKind.UTF_32, NativeCharsetKind.UTF_32BE, NativeCharsetKind.UTF_32LE -> {
                    val cr = encodeUtf32(`in`, out, ip, order)
                    if (cr != null) return cr
                    ip = `in`.position()
                    continue
                }
                NativeCharsetKind.UNKNOWN -> {
                    `in`.position(ip)
                    return CoderResult.unmappableForLength(1)
                }
            }
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }

    private fun encodeUtf8(`in`: CharBuffer, out: ByteBuffer, ip: Int): CoderResult? {
        val c = `in`.get(ip)
        val lim = `in`.limit()
        val cp: Int
        val consumed: Int
        if (c.isHighSurrogate()) {
            if (ip + 1 >= lim) {
                `in`.position(ip)
                return CoderResult.UNDERFLOW
            }
            val d = `in`.get(ip + 1)
            if (!d.isLowSurrogate()) {
                `in`.position(ip)
                return CoderResult.malformedForLength(1)
            }
            cp = ((c.code - 0xD800) shl 10) + (d.code - 0xDC00) + 0x10000
            consumed = 2
        } else if (c.isLowSurrogate()) {
            `in`.position(ip)
            return CoderResult.malformedForLength(1)
        } else {
            cp = c.code
            consumed = 1
        }
        val need = when {
            cp <= 0x7F -> 1
            cp <= 0x7FF -> 2
            cp <= 0xFFFF -> 3
            else -> 4
        }
        if (out.remaining() < need) {
            `in`.position(ip)
            return CoderResult.OVERFLOW
        }
        when (need) {
            1 -> out.put(cp.toByte())
            2 -> {
                out.put((0xC0 or (cp shr 6)).toByte())
                out.put((0x80 or (cp and 0x3F)).toByte())
            }
            3 -> {
                out.put((0xE0 or (cp shr 12)).toByte())
                out.put((0x80 or ((cp shr 6) and 0x3F)).toByte())
                out.put((0x80 or (cp and 0x3F)).toByte())
            }
            else -> {
                out.put((0xF0 or (cp shr 18)).toByte())
                out.put((0x80 or ((cp shr 12) and 0x3F)).toByte())
                out.put((0x80 or ((cp shr 6) and 0x3F)).toByte())
                out.put((0x80 or (cp and 0x3F)).toByte())
            }
        }
        `in`.position(ip + consumed)
        return null
    }

    private fun encodeUtf16(`in`: CharBuffer, out: ByteBuffer, ip: Int, order: ByteOrder): CoderResult? {
        val c = `in`.get(ip)
        val lim = `in`.limit()
        if (c.isHighSurrogate()) {
            if (ip + 1 >= lim) {
                `in`.position(ip)
                return CoderResult.UNDERFLOW
            }
            val d = `in`.get(ip + 1)
            if (!d.isLowSurrogate()) {
                `in`.position(ip)
                return CoderResult.malformedForLength(1)
            }
            if (out.remaining() < 4) {
                `in`.position(ip)
                return CoderResult.OVERFLOW
            }
            putCharBytes(out, c, order)
            putCharBytes(out, d, order)
            `in`.position(ip + 2)
            return null
        }
        if (c.isLowSurrogate()) {
            `in`.position(ip)
            return CoderResult.malformedForLength(1)
        }
        if (out.remaining() < 2) {
            `in`.position(ip)
            return CoderResult.OVERFLOW
        }
        putCharBytes(out, c, order)
        `in`.position(ip + 1)
        return null
    }

    private fun putCharBytes(out: ByteBuffer, c: Char, order: ByteOrder) {
        val v = c.code
        if (order == ByteOrder.BIG_ENDIAN) {
            out.put((v shr 8).toByte())
            out.put(v.toByte())
        } else {
            out.put(v.toByte())
            out.put((v shr 8).toByte())
        }
    }

    private fun encodeUtf32(`in`: CharBuffer, out: ByteBuffer, ip: Int, order: ByteOrder): CoderResult? {
        val c = `in`.get(ip)
        val lim = `in`.limit()
        val cp: Int
        val consumed: Int
        if (c.isHighSurrogate()) {
            if (ip + 1 >= lim) {
                `in`.position(ip)
                return CoderResult.UNDERFLOW
            }
            val d = `in`.get(ip + 1)
            if (!d.isLowSurrogate()) {
                `in`.position(ip)
                return CoderResult.malformedForLength(1)
            }
            cp = ((c.code - 0xD800) shl 10) + (d.code - 0xDC00) + 0x10000
            consumed = 2
        } else if (c.isLowSurrogate()) {
            `in`.position(ip)
            return CoderResult.malformedForLength(1)
        } else {
            cp = c.code
            consumed = 1
        }
        if (out.remaining() < 4) {
            `in`.position(ip)
            return CoderResult.OVERFLOW
        }
        putCodePointBytes(out, cp, order)
        `in`.position(ip + consumed)
        return null
    }

    private fun putCodePointBytes(out: ByteBuffer, cp: Int, order: ByteOrder) {
        if (order == ByteOrder.BIG_ENDIAN) {
            out.put((cp shr 24).toByte())
            out.put((cp shr 16).toByte())
            out.put((cp shr 8).toByte())
            out.put(cp.toByte())
        } else {
            out.put(cp.toByte())
            out.put((cp shr 8).toByte())
            out.put((cp shr 16).toByte())
            out.put((cp shr 24).toByte())
        }
    }
}

internal class NativeCharsetDecoder(
    cs: Charset,
    private val kind: NativeCharsetKind,
    avg: Float,
    max: Float
) : CharsetDecoder(cs, avg, max) {

    private var utf16Order: ByteOrder? = when (kind) {
        NativeCharsetKind.UTF_16BE -> ByteOrder.BIG_ENDIAN
        NativeCharsetKind.UTF_16LE -> ByteOrder.LITTLE_ENDIAN
        NativeCharsetKind.UTF_16 -> null
        else -> null
    }
    private var bomSeen = kind != NativeCharsetKind.UTF_16
    private var utf32Order: ByteOrder? = when (kind) {
        NativeCharsetKind.UTF_32BE -> ByteOrder.BIG_ENDIAN
        NativeCharsetKind.UTF_32LE -> ByteOrder.LITTLE_ENDIAN
        NativeCharsetKind.UTF_32 -> null
        else -> null
    }
    private var bomSeen32 = kind != NativeCharsetKind.UTF_32

    override fun implReset() {
        utf16Order = when (kind) {
            NativeCharsetKind.UTF_16BE -> ByteOrder.BIG_ENDIAN
            NativeCharsetKind.UTF_16LE -> ByteOrder.LITTLE_ENDIAN
            else -> null
        }
        bomSeen = kind != NativeCharsetKind.UTF_16
        utf32Order = when (kind) {
            NativeCharsetKind.UTF_32BE -> ByteOrder.BIG_ENDIAN
            NativeCharsetKind.UTF_32LE -> ByteOrder.LITTLE_ENDIAN
            else -> null
        }
        bomSeen32 = kind != NativeCharsetKind.UTF_32
    }

    override fun decodeLoop(`in`: ByteBuffer, out: CharBuffer): CoderResult {
        when (kind) {
            NativeCharsetKind.US_ASCII -> return decodeSingleByte(`in`, out, 0x80)
            NativeCharsetKind.ISO_8859_1 -> return decodeLatin1(`in`, out)
            NativeCharsetKind.UTF_8 -> return decodeUtf8(`in`, out)
            NativeCharsetKind.UTF_16, NativeCharsetKind.UTF_16BE, NativeCharsetKind.UTF_16LE ->
                return decodeUtf16(`in`, out)
            NativeCharsetKind.UTF_32, NativeCharsetKind.UTF_32BE, NativeCharsetKind.UTF_32LE ->
                return decodeUtf32(`in`, out)
            NativeCharsetKind.UNKNOWN -> return CoderResult.malformedForLength(1)
        }
    }

    private fun decodeSingleByte(`in`: ByteBuffer, out: CharBuffer, limit: Int): CoderResult {
        var ip = `in`.position()
        val ilim = `in`.limit()
        while (ip < ilim) {
            if (!out.hasRemaining()) {
                `in`.position(ip)
                return CoderResult.OVERFLOW
            }
            val b = `in`.get(ip).toInt() and 0xff
            if (b >= limit) {
                `in`.position(ip)
                return CoderResult.malformedForLength(1)
            }
            out.put(b.toChar())
            ip++
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }

    private fun decodeLatin1(`in`: ByteBuffer, out: CharBuffer): CoderResult {
        var ip = `in`.position()
        val ilim = `in`.limit()
        while (ip < ilim) {
            if (!out.hasRemaining()) {
                `in`.position(ip)
                return CoderResult.OVERFLOW
            }
            out.put((`in`.get(ip).toInt() and 0xff).toChar())
            ip++
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }

    private fun decodeUtf8(`in`: ByteBuffer, out: CharBuffer): CoderResult {
        var ip = `in`.position()
        val ilim = `in`.limit()
        while (ip < ilim) {
            val b1 = `in`.get(ip).toInt() and 0xff
            val need: Int
            val minCp: Int
            when {
                b1 and 0x80 == 0 -> {
                    need = 1
                    minCp = 0
                }
                b1 and 0xE0 == 0xC0 -> {
                    need = 2
                    minCp = 0x80
                }
                b1 and 0xF0 == 0xE0 -> {
                    need = 3
                    minCp = 0x800
                }
                b1 and 0xF8 == 0xF0 -> {
                    need = 4
                    minCp = 0x10000
                }
                else -> {
                    `in`.position(ip)
                    return CoderResult.malformedForLength(1)
                }
            }
            if (ip + need > ilim) {
                `in`.position(ip)
                return CoderResult.UNDERFLOW
            }
            var cp = when (need) {
                1 -> b1
                2 -> b1 and 0x1F
                3 -> b1 and 0x0F
                else -> b1 and 0x07
            }
            for (i in 1 until need) {
                val b = `in`.get(ip + i).toInt() and 0xff
                if (b and 0xC0 != 0x80) {
                    `in`.position(ip)
                    return CoderResult.malformedForLength(1)
                }
                cp = (cp shl 6) or (b and 0x3F)
            }
            if (cp < minCp || cp > 0x10FFFF || (cp in 0xD800..0xDFFF)) {
                `in`.position(ip)
                return CoderResult.malformedForLength(need)
            }
            val chars = if (cp <= 0xFFFF) 1 else 2
            if (out.remaining() < chars) {
                `in`.position(ip)
                return CoderResult.OVERFLOW
            }
            if (cp <= 0xFFFF) {
                out.put(cp.toChar())
            } else {
                val x = cp - 0x10000
                out.put(((x shr 10) + 0xD800).toChar())
                out.put(((x and 0x3FF) + 0xDC00).toChar())
            }
            ip += need
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }

    private fun decodeUtf16(`in`: ByteBuffer, out: CharBuffer): CoderResult {
        var ip = `in`.position()
        val ilim = `in`.limit()
        if (!bomSeen && ilim - ip >= 2) {
            val b0 = `in`.get(ip).toInt() and 0xff
            val b1 = `in`.get(ip + 1).toInt() and 0xff
            when {
                b0 == 0xFE && b1 == 0xFF -> {
                    utf16Order = ByteOrder.BIG_ENDIAN
                    ip += 2
                }
                b0 == 0xFF && b1 == 0xFE -> {
                    utf16Order = ByteOrder.LITTLE_ENDIAN
                    ip += 2
                }
                else -> utf16Order = ByteOrder.BIG_ENDIAN
            }
            bomSeen = true
        }
        val order = utf16Order ?: ByteOrder.BIG_ENDIAN
        while (ip + 1 < ilim) {
            val c = readUtf16Char(`in`, ip, order)
            if (c.isHighSurrogate()) {
                if (ip + 3 >= ilim) {
                    `in`.position(ip)
                    return CoderResult.UNDERFLOW
                }
                val d = readUtf16Char(`in`, ip + 2, order)
                if (!d.isLowSurrogate()) {
                    `in`.position(ip)
                    return CoderResult.malformedForLength(2)
                }
                if (out.remaining() < 2) {
                    `in`.position(ip)
                    return CoderResult.OVERFLOW
                }
                out.put(c)
                out.put(d)
                ip += 4
            } else if (c.isLowSurrogate()) {
                `in`.position(ip)
                return CoderResult.malformedForLength(2)
            } else {
                if (!out.hasRemaining()) {
                    `in`.position(ip)
                    return CoderResult.OVERFLOW
                }
                out.put(c)
                ip += 2
            }
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }

    private fun readUtf16Char(buf: ByteBuffer, index: Int, order: ByteOrder): Char {
        val b0 = buf.get(index).toInt() and 0xff
        val b1 = buf.get(index + 1).toInt() and 0xff
        val v = if (order == ByteOrder.BIG_ENDIAN) (b0 shl 8) or b1 else (b1 shl 8) or b0
        return v.toChar()
    }

    private fun decodeUtf32(`in`: ByteBuffer, out: CharBuffer): CoderResult {
        var ip = `in`.position()
        val ilim = `in`.limit()
        if (!bomSeen32 && ilim - ip >= 4) {
            val b0 = `in`.get(ip).toInt() and 0xff
            val b1 = `in`.get(ip + 1).toInt() and 0xff
            val b2 = `in`.get(ip + 2).toInt() and 0xff
            val b3 = `in`.get(ip + 3).toInt() and 0xff
            when {
                b0 == 0x00 && b1 == 0x00 && b2 == 0xFE && b3 == 0xFF -> {
                    utf32Order = ByteOrder.BIG_ENDIAN
                    ip += 4
                }
                b0 == 0xFF && b1 == 0xFE && b2 == 0x00 && b3 == 0x00 -> {
                    utf32Order = ByteOrder.LITTLE_ENDIAN
                    ip += 4
                }
                else -> utf32Order = ByteOrder.BIG_ENDIAN
            }
            bomSeen32 = true
        }
        val order = utf32Order ?: ByteOrder.BIG_ENDIAN
        while (ip + 3 < ilim) {
            val b0 = `in`.get(ip).toInt() and 0xff
            val b1 = `in`.get(ip + 1).toInt() and 0xff
            val b2 = `in`.get(ip + 2).toInt() and 0xff
            val b3 = `in`.get(ip + 3).toInt() and 0xff
            val cp = if (order == ByteOrder.BIG_ENDIAN) {
                (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
            } else {
                (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
            }
            if (cp < 0 || cp > 0x10FFFF || (cp in 0xD800..0xDFFF)) {
                `in`.position(ip)
                return CoderResult.malformedForLength(4)
            }
            val charsNeeded = if (cp <= 0xFFFF) 1 else 2
            if (out.remaining() < charsNeeded) {
                `in`.position(ip)
                return CoderResult.OVERFLOW
            }
            if (cp <= 0xFFFF) {
                out.put(cp.toChar())
            } else {
                val x = cp - 0x10000
                out.put(((x shr 10) + 0xD800).toChar())
                out.put(((x and 0x3FF) + 0xDC00).toChar())
            }
            ip += 4
        }
        `in`.position(ip)
        return CoderResult.UNDERFLOW
    }
}
