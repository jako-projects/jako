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
}
