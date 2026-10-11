package jako.test.io

import java.io.*

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.*

class IOStreamTest {

    @Test
    fun testByteArrayInputStreamReadSingleBytes() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val stream = ByteArrayInputStream(data)

        assertEquals(5, stream.available())
        assertEquals(10, stream.read())
        assertEquals(20, stream.read())
        assertEquals(30, stream.read())
        assertEquals(40, stream.read())
        assertEquals(50, stream.read())
        assertEquals(-1, stream.read())
        assertEquals(0, stream.available())
    }

    @Test
    fun testByteArrayInputStreamReadArray() {
        val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val stream = ByteArrayInputStream(data, 2, 4) // sub-array: [3, 4, 5, 6]

        val buffer = ByteArray(10)
        val readCount = stream.read(buffer, 0, 10)

        assertEquals(4, readCount)
        assertEquals(3, buffer[0].toInt())
        assertEquals(4, buffer[1].toInt())
        assertEquals(5, buffer[2].toInt())
        assertEquals(6, buffer[3].toInt())
        assertEquals(-1, stream.read())
    }

    @Test
    fun testByteArrayInputStreamMarkAndReset() {
        val data = byteArrayOf(10, 20, 30, 40, 50)
        val stream = ByteArrayInputStream(data)

        assertTrue(stream.markSupported())
        assertEquals(10, stream.read())
        assertEquals(20, stream.read())

        stream.mark(0)
        assertEquals(30, stream.read())
        assertEquals(40, stream.read())

        stream.reset()
        assertEquals(30, stream.read())
        assertEquals(40, stream.read())
        assertEquals(50, stream.read())
        assertEquals(-1, stream.read())
    }

    @Test
    fun testByteArrayOutputStreamWrite() {
        val out = ByteArrayOutputStream()
        out.write(65) // 'A'
        out.write(66) // 'B'
        out.write(67) // 'C'

        assertEquals(3, out.size())
        val bytes = out.toByteArray()
        assertEquals(3, bytes.size)
        assertEquals(65, bytes[0].toInt())
        assertEquals(66, bytes[1].toInt())
        assertEquals(67, bytes[2].toInt())

        assertEquals("ABC", out.toString())
    }

    @Test
    fun testByteArrayOutputStreamWriteArray() {
        val out = ByteArrayOutputStream(16)
        val src = byteArrayOf(100, 101, 102, 103, 104)
        out.write(src, 1, 3) // writes [101, 102, 103]

        assertEquals(3, out.size())
        val bytes = out.toByteArray()
        assertEquals(101, bytes[0].toInt())
        assertEquals(102, bytes[1].toInt())
        assertEquals(103, bytes[2].toInt())

        out.reset()
        assertEquals(0, out.size())
        assertEquals(0, out.toByteArray().size)
    }

    @Test
    fun testExceptionsHierarchy() {
        val ioEx: IOException = FileNotFoundException("file not found")
        assertEquals("file not found", ioEx.message)

        val eofEx: IOException = EOFException("unexpected eof")
        assertEquals("unexpected eof", eofEx.message)
    }

    @Test
    fun testStandardCharsets() {
        assertEquals("UTF-8", StandardCharsets.UTF_8.name())
        assertEquals("US-ASCII", StandardCharsets.US_ASCII.name())
        assertEquals("ISO-8859-1", StandardCharsets.ISO_8859_1.name())
        assertEquals("UTF-16", StandardCharsets.UTF_16.name())

        val utf8 = Charset.forName("UTF-8")
        assertEquals("UTF-8", utf8.name())
    }
}
