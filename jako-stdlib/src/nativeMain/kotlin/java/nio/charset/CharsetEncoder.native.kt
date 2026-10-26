package java.nio.charset

import java.nio.ByteBuffer
import java.nio.CharBuffer

internal const val ST_RESET = 0

internal const val ST_CODING = 1

internal const val ST_END = 2

internal const val ST_FLUSHED = 3

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
        if (newReplacement.isEmpty() || newReplacement.size > max) {
            throw IllegalArgumentException("Illegal replacement")
        }
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
        if (state != ST_END) {
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
