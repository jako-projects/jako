package jako.test.io

import java.io.*
import kotlin.test.*

class PushbackStreamTest {

    @Test
    fun testPushbackInputStreamSingleByte() {
        val src = ByteArrayInputStream(byteArrayOf(10, 20, 30))
        val pbis = PushbackInputStream(src, 2)

        assertEquals(10, pbis.read())
        pbis.unread(99)
        assertEquals(99, pbis.read())
        assertEquals(20, pbis.read())
        assertEquals(30, pbis.read())
        assertEquals(-1, pbis.read())
    }

    @Test
    fun testPushbackInputStreamByteArray() {
        val src = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4, 5))
        val pbis = PushbackInputStream(src, 5)

        val buf = ByteArray(3)
        assertEquals(3, pbis.read(buf))
        assertEquals(1, buf[0].toInt())
        assertEquals(2, buf[1].toInt())
        assertEquals(3, buf[2].toInt())

        pbis.unread(byteArrayOf(10, 20))
        assertEquals(10, pbis.read())
        assertEquals(20, pbis.read())
        assertEquals(4, pbis.read())
        assertEquals(5, pbis.read())
        assertEquals(-1, pbis.read())
    }

    @Test
    fun testPushbackReader() {
        val src = StringReader("hello")
        val pbr = PushbackReader(src, 5)

        assertEquals('h'.code, pbr.read())
        pbr.unread('X'.code)
        assertEquals('X'.code, pbr.read())
        assertEquals('e'.code, pbr.read())
        assertEquals('l'.code, pbr.read())
    }
}
