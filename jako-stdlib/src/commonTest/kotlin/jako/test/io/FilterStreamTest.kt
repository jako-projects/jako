package jako.test.io

import java.io.*
import kotlin.test.*

class FilterStreamTest {

    @Test
    fun testFilterInputStream() {
        val src = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4, 5))
        class MyFilter(inStream: InputStream) : FilterInputStream(inStream)
        val filter = MyFilter(src)

        assertEquals(5, filter.available())
        assertEquals(1, filter.read())
        val buf = ByteArray(3)
        assertEquals(3, filter.read(buf))
        assertEquals(2, buf[0].toInt())
        assertEquals(3, buf[1].toInt())
        assertEquals(4, buf[2].toInt())
        assertEquals(5, filter.read())
        assertEquals(-1, filter.read())
        filter.close()
    }

    @Test
    fun testFilterOutputStream() {
        val baos = ByteArrayOutputStream()
        class MyFilter(out: OutputStream) : FilterOutputStream(out)
        val filter = MyFilter(baos)

        filter.write(65)
        filter.write(byteArrayOf(66, 67, 68))
        filter.flush()

        val bytes = baos.toByteArray()
        assertEquals(4, bytes.size)
        assertEquals(65, bytes[0].toInt())
        assertEquals(66, bytes[1].toInt())
        assertEquals(67, bytes[2].toInt())
        assertEquals(68, bytes[3].toInt())
        filter.close()
    }

    @Test
    fun testBufferedInputStreamAndBufferedOutputStream() {
        assertFailsWith<IllegalArgumentException> {
            BufferedInputStream(ByteArrayInputStream(ByteArray(0)), 0)
        }
        assertFailsWith<IllegalArgumentException> {
            BufferedOutputStream(ByteArrayOutputStream(), -1)
        }

        val baos = ByteArrayOutputStream()
        val bos = BufferedOutputStream(baos, 4)
        bos.write('A'.code)
        bos.write("BCDEFG".encodeToByteArray(), 0, 6)
        bos.flush()
        bos.close()
        assertEquals("ABCDEFG", baos.toByteArray().decodeToString())

        val bis = BufferedInputStream(ByteArrayInputStream(baos.toByteArray()), 4)
        assertTrue(bis.markSupported())
        assertEquals('A'.code, bis.read())
        bis.mark(10)
        val chunk = ByteArray(3)
        assertEquals(3, bis.read(chunk, 0, 3))
        assertEquals("BCD", chunk.decodeToString())
        bis.reset()
        assertEquals('B'.code, bis.read())
        assertEquals(2L, bis.skip(2L))
        assertEquals('E'.code, bis.read())
        assertTrue(bis.available() >= 2)
        bis.close()
    }
}
