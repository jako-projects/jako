package jako.test.nio

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.nio.charset.CoderResult
import java.nio.charset.CodingErrorAction
import java.nio.charset.MalformedInputException
import java.nio.charset.StandardCharsets
import java.nio.charset.UnmappableCharacterException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CharsetCoderTest {

    @Test
    fun testEncodeUtf8Chinese() {
        val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder()
        val encoded = encoder.encode(CharBuffer.wrap("你好"))
        val bytes = ByteArray(encoded.remaining())
        encoded.get(bytes)
        assertTrue(
            bytes.contentEquals(
                byteArrayOf(
                    0xE4.toByte(), 0xBD.toByte(), 0xA0.toByte(),
                    0xE5.toByte(), 0xA5.toByte(), 0xBD.toByte()
                )
            )
        )

        val decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()
        val decoded = decoder.decode(ByteBuffer.wrap(bytes))
        assertEquals("你好", decoded.toString())

        val viaCharset = Charset.forName("UTF-8").encode("你好")
        val viaBytes = ByteArray(viaCharset.remaining())
        viaCharset.get(viaBytes)
        assertTrue(viaBytes.contentEquals(bytes))
    }

    @Test
    fun testUsAsciiAndIso88591() {
        val ascii = StandardCharsets.US_ASCII.newEncoder().encode(CharBuffer.wrap("Hello"))
        val asciiBytes = ByteArray(ascii.remaining())
        ascii.get(asciiBytes)
        assertTrue(asciiBytes.contentEquals(byteArrayOf(0x48, 0x65, 0x6C, 0x6C, 0x6F)))

        val latin = StandardCharsets.ISO_8859_1.newEncoder().encode(CharBuffer.wrap("caf\u00E9"))
        val latinBytes = ByteArray(latin.remaining())
        latin.get(latinBytes)
        assertTrue(latinBytes.contentEquals(byteArrayOf(0x63, 0x61, 0x66, 0xE9.toByte())))

        val decodedLatin = StandardCharsets.ISO_8859_1.newDecoder().decode(ByteBuffer.wrap(latinBytes))
        assertEquals("caf\u00E9", decodedLatin.toString())
    }

    @Test
    fun testUtf16Family() {
        val utf16 = StandardCharsets.UTF_16.newEncoder().encode(CharBuffer.wrap("A"))
        val utf16Bytes = ByteArray(utf16.remaining())
        utf16.get(utf16Bytes)
        assertTrue(utf16Bytes.contentEquals(byteArrayOf(0xFE.toByte(), 0xFF.toByte(), 0x00, 0x41)))

        val be = StandardCharsets.UTF_16BE.newEncoder().encode(CharBuffer.wrap("A"))
        val beBytes = ByteArray(be.remaining())
        be.get(beBytes)
        assertTrue(beBytes.contentEquals(byteArrayOf(0x00, 0x41)))

        val le = StandardCharsets.UTF_16LE.newEncoder().encode(CharBuffer.wrap("A"))
        val leBytes = ByteArray(le.remaining())
        le.get(leBytes)
        assertTrue(leBytes.contentEquals(byteArrayOf(0x41, 0x00)))

        val decoded = StandardCharsets.UTF_16.newDecoder().decode(ByteBuffer.wrap(utf16Bytes))
        assertEquals("A", decoded.toString())
    }

    @Test
    fun testCoderResultUnderflowOverflow() {
        val encoder = StandardCharsets.UTF_8.newEncoder()
        val small = ByteBuffer.allocate(1)
        val cr = encoder.encode(CharBuffer.wrap("你好"), small, true)
        assertTrue(cr.isOverflow)
        assertTrue(CoderResult.OVERFLOW.isOverflow)
        assertTrue(CoderResult.UNDERFLOW.isUnderflow)
        assertFalseResult()
    }

    private fun assertFalseResult() {
        assertTrue(!CoderResult.UNDERFLOW.isError)
        assertTrue(!CoderResult.OVERFLOW.isMalformed)
    }

    @Test
    fun testCharsetForNameAndValidation() {
        assertEquals("UTF-8", Charset.forName("utf-8").name())
        assertEquals("UTF-8", Charset.forName("UTF-8").name())
        assertEquals("US-ASCII", Charset.forName("us-ascii").name())
        assertEquals("ISO-8859-1", Charset.forName("iso-8859-1").name())
        assertEquals("UTF-16", Charset.forName("utf-16").name())
        assertEquals("UTF-16BE", Charset.forName("utf-16be").name())
        assertEquals("UTF-16LE", Charset.forName("utf-16le").name())

        assertTrue(Charset.isSupported("utf-8"))
        assertTrue(Charset.isSupported("US-ASCII"))
        assertTrue(Charset.isSupported("ISO-8859-1"))
        assertTrue(!Charset.isSupported("unknown-charset"))

        kotlin.test.assertFailsWith<java.nio.charset.UnsupportedCharsetException> {
            Charset.forName("utf-wtf")
        }

        kotlin.test.assertFailsWith<java.nio.charset.IllegalCharsetNameException> {
            Charset.forName("")
        }

        kotlin.test.assertFailsWith<java.nio.charset.IllegalCharsetNameException> {
            Charset.forName("!@#$%^&*()")
        }

        kotlin.test.assertFailsWith<java.nio.charset.IllegalCharsetNameException> {
            Charset.forName("'utf-8'")
        }
    }

    @Test
    fun testUtf32Coder() {
        val be = Charset.forName("UTF-32BE").newEncoder().encode(CharBuffer.wrap("A"))
        val beBytes = ByteArray(be.remaining())
        be.get(beBytes)
        assertTrue(beBytes.contentEquals(byteArrayOf(0x00, 0x00, 0x00, 0x41)))

        val le = Charset.forName("UTF-32LE").newEncoder().encode(CharBuffer.wrap("A"))
        val leBytes = ByteArray(le.remaining())
        le.get(leBytes)
        assertTrue(leBytes.contentEquals(byteArrayOf(0x41, 0x00, 0x00, 0x00)))

        val decodedBe = Charset.forName("UTF-32BE").newDecoder().decode(ByteBuffer.wrap(beBytes))
        assertEquals("A", decodedBe.toString())

        val decodedLe = Charset.forName("UTF-32LE").newDecoder().decode(ByteBuffer.wrap(leBytes))
        assertEquals("A", decodedLe.toString())
    }

    @Test
    fun testCharsetEncoderDecoderAndCodingErrorActions() {
        assertEquals("REPORT", CodingErrorAction.REPORT.toString())
        assertEquals("REPLACE", CodingErrorAction.REPLACE.toString())
        assertEquals("IGNORE", CodingErrorAction.IGNORE.toString())

        // 1. Decoder properties, replacements, and malformed input actions
        val dec: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()
        assertEquals(StandardCharsets.UTF_8, dec.charset())
        assertEquals("\uFFFD", dec.replacement())
        assertSame(CodingErrorAction.REPORT, dec.malformedInputAction())
        assertSame(CodingErrorAction.REPORT, dec.unmappableCharacterAction())
        assertTrue(dec.averageCharsPerByte() > 0f)
        assertTrue(dec.maxCharsPerByte() >= dec.averageCharsPerByte())

        assertFailsWith<IllegalArgumentException> { dec.replaceWith("") }
        assertFailsWith<IllegalArgumentException> { dec.replaceWith("TOO_LONG") }
        dec.replaceWith("?")
        assertEquals("?", dec.replacement())

        val malformedUtf8 = byteArrayOf(0x41, 0xFF.toByte(), 0x42)
        // REPORT throws MalformedInputException on convenience decode(ByteBuffer)
        assertFailsWith<MalformedInputException> {
            dec.decode(ByteBuffer.wrap(malformedUtf8))
        }

        // REPLACE substitutes replacement string
        dec.onMalformedInput(CodingErrorAction.REPLACE)
        assertSame(CodingErrorAction.REPLACE, dec.malformedInputAction())
        assertEquals("A?B", dec.decode(ByteBuffer.wrap(malformedUtf8)).toString())

        // IGNORE skips malformed bytes
        dec.onMalformedInput(CodingErrorAction.IGNORE)
        assertSame(CodingErrorAction.IGNORE, dec.malformedInputAction())
        assertEquals("AB", dec.decode(ByteBuffer.wrap(malformedUtf8)).toString())

        // Truncated multi-byte sequence at endOfInput=true
        dec.onMalformedInput(CodingErrorAction.REPORT)
        dec.reset()
        assertFailsWith<IllegalStateException> {
            dec.flush(CharBuffer.allocate(4))
        }
        val truncated = ByteBuffer.wrap(byteArrayOf(0xE4.toByte(), 0xBD.toByte()))
        val outChars = CharBuffer.allocate(4)
        val step1 = dec.decode(truncated, outChars, false)
        assertTrue(step1.isUnderflow)
        assertFailsWith<IllegalStateException> {
            dec.flush(outChars)
        }
        val step2 = dec.decode(truncated, outChars, true)
        assertTrue(step2.isMalformed)
        assertEquals(2, step2.length())

        // 2. Encoder properties, replacements, and unmappable/malformed actions
        val enc: CharsetEncoder = StandardCharsets.US_ASCII.newEncoder()
        assertEquals(StandardCharsets.US_ASCII, enc.charset())
        assertTrue(enc.replacement().contentEquals(byteArrayOf('?'.code.toByte())))
        assertSame(CodingErrorAction.REPORT, enc.malformedInputAction())
        assertSame(CodingErrorAction.REPORT, enc.unmappableCharacterAction())
        assertTrue(enc.averageBytesPerChar() > 0f)
        assertTrue(enc.maxBytesPerChar() >= enc.averageBytesPerChar())

        assertFailsWith<IllegalArgumentException> { enc.replaceWith(byteArrayOf()) }
        assertFailsWith<IllegalArgumentException> { enc.replaceWith(byteArrayOf(1, 2, 3)) }
        enc.replaceWith(byteArrayOf('_'.code.toByte()))
        assertTrue(enc.replacement().contentEquals(byteArrayOf('_'.code.toByte())))
        assertFailsWith<IllegalStateException> {
            enc.flush(ByteBuffer.allocate(4))
        }

        // Unmappable character in US_ASCII
        assertFailsWith<UnmappableCharacterException> {
            enc.encode(CharBuffer.wrap("A中B"))
        }

        enc.onUnmappableCharacter(CodingErrorAction.REPLACE)
        assertSame(CodingErrorAction.REPLACE, enc.unmappableCharacterAction())
        val replacedBuf = enc.encode(CharBuffer.wrap("A中B"))
        val replacedBytes = ByteArray(replacedBuf.remaining()).also { replacedBuf.get(it) }
        assertEquals("A_B", replacedBytes.decodeToString())

        enc.onUnmappableCharacter(CodingErrorAction.IGNORE)
        assertSame(CodingErrorAction.IGNORE, enc.unmappableCharacterAction())
        val ignoredBuf = enc.encode(CharBuffer.wrap("A中B"))
        val ignoredBytes = ByteArray(ignoredBuf.remaining()).also { ignoredBuf.get(it) }
        assertEquals("AB", ignoredBytes.decodeToString())

        // Unpaired surrogate in UTF-8 encoder
        val utf8Enc = StandardCharsets.UTF_8.newEncoder()
        utf8Enc.onMalformedInput(CodingErrorAction.REPORT)
        assertFailsWith<MalformedInputException> {
            utf8Enc.encode(CharBuffer.wrap(charArrayOf('A', '\uD800', 'B')))
        }
        utf8Enc.onMalformedInput(CodingErrorAction.IGNORE)
        val surrIgnored = utf8Enc.encode(CharBuffer.wrap(charArrayOf('A', '\uD800', 'B')))
        val surrBytes = ByteArray(surrIgnored.remaining()).also { surrIgnored.get(it) }
        assertEquals("AB", surrBytes.decodeToString())
    }
}

