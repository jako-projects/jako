package jako.test.io

import java.io.*
import kotlin.test.*

class PipedStreamTest {

    @Test
    fun testPipedInputOutputStream() {
        val out = PipedOutputStream()
        val inStream = PipedInputStream(out)

        out.write(byteArrayOf(10, 20, 30))
        assertEquals(3, inStream.available())

        val buf = ByteArray(3)
        assertEquals(3, inStream.read(buf))
        assertEquals(10, buf[0].toInt())
        assertEquals(20, buf[1].toInt())
        assertEquals(30, buf[2].toInt())

        out.close()
        assertEquals(-1, inStream.read())
        inStream.close()
    }

    @Test
    fun testPipedReaderWriter() {
        val writer = PipedWriter()
        val reader = PipedReader(writer)

        writer.write("Hello Pipe")
        assertTrue(reader.ready())

        val buf = CharArray(10)
        val readLen = reader.read(buf, 0, 10)
        assertEquals(10, readLen)
        assertEquals("Hello Pipe", buf.concatToString(0, 10))

        writer.close()
        assertEquals(-1, reader.read())
        reader.close()
    }
}
