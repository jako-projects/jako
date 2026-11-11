package jako.test.zip

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.CheckedOutputStream
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterOutputStream
import java.util.zip.ZipException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ZipFilterStreamsTest {

    @Test
    fun testCheckedInputAndOutputStreams() {
        val payload = "Hello Checked Streams! 1234567890".encodeToByteArray()
        val outCrc = CRC32()
        val baos = ByteArrayOutputStream()
        val checkedOut = CheckedOutputStream(baos, outCrc)
        checkedOut.write(payload[0].toInt() and 0xFF)
        checkedOut.write(payload, 1, payload.size - 1)
        checkedOut.close()

        val expectedCrc = CRC32().apply { update(payload, 0, payload.size) }.value
        assertEquals(expectedCrc, checkedOut.checksum.value)
        assertContentEquals(payload, baos.toByteArray())

        val inAdler = Adler32()
        val checkedIn = CheckedInputStream(ByteArrayInputStream(baos.toByteArray()), inAdler)
        val first = checkedIn.read()
        assertEquals(payload[0].toInt() and 0xFF, first)
        val rest = ByteArray(payload.size - 1)
        var offset = 0
        while (offset < rest.size) {
            val n = checkedIn.read(rest, offset, rest.size - offset)
            if (n < 0) break
            offset += n
        }
        assertEquals(rest.size, offset)
        assertEquals(-1, checkedIn.read())
        checkedIn.close()

        val expectedAdler = Adler32().apply { update(payload, 0, payload.size) }.value
        assertEquals(expectedAdler, checkedIn.checksum.value)
    }

    @Test
    fun testDeflaterInputStreamAndInflaterOutputStream() {
        val original = ("The quick brown fox jumps over the lazy dog. ".repeat(10)).encodeToByteArray()

        // Compress using DeflaterInputStream
        val defIn = DeflaterInputStream(ByteArrayInputStream(original))
        assertFalse(defIn.markSupported())
        val compressedOut = ByteArrayOutputStream()
        val buf = ByteArray(64)
        while (true) {
            val n = defIn.read(buf, 0, buf.size)
            if (n < 0) break
            compressedOut.write(buf, 0, n)
        }
        assertEquals(0, defIn.available())
        defIn.close()
        val compressedBytes = compressedOut.toByteArray()

        // Decompress using InflaterOutputStream
        val decompressedOut = ByteArrayOutputStream()
        val infOut = InflaterOutputStream(decompressedOut)
        infOut.write(compressedBytes[0].toInt() and 0xFF)
        infOut.write(compressedBytes, 1, compressedBytes.size - 1)
        infOut.finish()
        infOut.close()

        assertContentEquals(original, decompressedOut.toByteArray())
    }

    @Test
    fun testCheckedStreamsWithoutBytes() {
        val crc = CRC32()
        val checkedCrc = CheckedOutputStream(ByteArrayOutputStream(), crc)
        checkedCrc.close()
        assertEquals(0L, checkedCrc.checksum.value)

        val adler = Adler32()
        val checkedAdler = CheckedOutputStream(ByteArrayOutputStream(), adler)
        checkedAdler.close()
        assertEquals(1L, checkedAdler.checksum.value)

        val emptyCrc = CRC32()
        val emptyIn = CheckedInputStream(ByteArrayInputStream(ByteArray(0)), emptyCrc)
        try {
            assertEquals(-1, emptyIn.read())
            assertEquals(0L, emptyIn.checksum.value)
        } finally {
            emptyIn.close()
        }

        val data = "123456789".encodeToByteArray()
        val knownCrc = CRC32()
        val knownIn = CheckedInputStream(ByteArrayInputStream(data), knownCrc)
        try {
            val buf = ByteArray(data.size)
            assertEquals(data.size, knownIn.read(buf, 0, buf.size))
            assertEquals(3421780262L, knownIn.checksum.value)
        } finally {
            knownIn.close()
        }
    }

    @Test
    fun testEmptyDeflaterInputStreamRoundtrip() {
        val defIn = DeflaterInputStream(ByteArrayInputStream(ByteArray(0)))
        val compressedOut = ByteArrayOutputStream()
        try {
            val buf = ByteArray(64)
            while (true) {
                val n = defIn.read(buf, 0, buf.size)
                if (n < 0) break
                compressedOut.write(buf, 0, n)
            }
        } finally {
            defIn.close()
        }

        val decompressedOut = ByteArrayOutputStream()
        val infOut = InflaterOutputStream(decompressedOut)
        try {
            infOut.write(compressedOut.toByteArray())
            infOut.finish()
        } finally {
            infOut.close()
        }
        assertContentEquals(ByteArray(0), decompressedOut.toByteArray())
    }

    @Test
    fun testInflaterOutputStreamPlainBytesThrowZipException() {
        val infOut = InflaterOutputStream(ByteArrayOutputStream())
        try {
            assertFailsWith<ZipException> {
                infOut.write(byteArrayOf(1, 2, 3, 4))
            }
            assertFailsWith<ZipException> {
                infOut.finish()
            }
        } finally {
            try {
                infOut.close()
            } catch (_: ZipException) {
            }
        }
    }

    @Test
    fun testDeflaterInputStreamSkipNegative() {
        val defIn = DeflaterInputStream(ByteArrayInputStream(ByteArray(0)))
        try {
            assertFailsWith<IllegalArgumentException> {
                defIn.skip(-1L)
            }
        } finally {
            defIn.close()
        }
    }
}
