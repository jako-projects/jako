package jako.test.io

import java.io.*
import kotlin.test.*

class CharArrayStreamTest {

    @Test
    fun testCharArrayReader() {
        val chars = charArrayOf('A', 'B', 'C', 'D', 'E')
        val reader = CharArrayReader(chars, 1, 3)

        assertTrue(reader.ready())
        assertEquals('B'.code, reader.read())
        assertEquals('C'.code, reader.read())
        assertEquals('D'.code, reader.read())
        assertEquals(-1, reader.read())
    }

    @Test
    fun testCharArrayWriter() {
        val writer = CharArrayWriter()
        writer.write('H'.code)
        writer.write("ello".toCharArray())
        writer.write(", World!")

        assertEquals(13, writer.size())
        assertEquals("Hello, World!", writer.toString())

        val sw = StringWriter()
        writer.writeTo(sw)
        assertEquals("Hello, World!", sw.toString())

        writer.reset()
        assertEquals(0, writer.size())
    }
}
