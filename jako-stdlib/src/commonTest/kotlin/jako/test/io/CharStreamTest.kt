package jako.test.io

import java.io.*

import java.nio.charset.StandardCharsets
import kotlin.test.*

class CharStreamTest {

    @Test
    fun testStringReaderAndStringWriter() {
        val input = "Hello, Jako Multiplatform World!\nLine 2"
        val reader = StringReader(input)
        val writer = StringWriter()

        val buf = CharArray(10)
        var n: Int
        while (reader.read(buf, 0, 10).also { n = it } != -1) {
            writer.write(buf, 0, n)
        }

        assertEquals(input, writer.toString())
    }

    @Test
    fun testBufferedReaderReadLine() {
        val text = "Line 1\r\nLine 2\nLine 3\rLine 4"
        val reader = BufferedReader(StringReader(text))

        assertEquals("Line 1", reader.readLine())
        assertEquals("Line 2", reader.readLine())
        assertEquals("Line 3", reader.readLine())
        assertEquals("Line 4", reader.readLine())
        assertNull(reader.readLine())
    }

    @Test
    fun testBufferedWriterNewLine() {
        val sw = StringWriter()
        val bw = BufferedWriter(sw)

        bw.write("Hello")
        bw.newLine()
        bw.write("World")
        bw.flush()

        assertEquals("Hello\nWorld", sw.toString().replace("\r\n", "\n"))
    }

    @Test
    fun testInputStreamReaderAndOutputStreamWriter() {
        val bos = ByteArrayOutputStream()
        val osw = OutputStreamWriter(bos, StandardCharsets.UTF_8)
        osw.write("Testing UTF-8 编码: 🚀 Hello World")
        osw.flush()

        val bytes = bos.toByteArray()
        val bis = ByteArrayInputStream(bytes)
        val isr = InputStreamReader(bis, StandardCharsets.UTF_8)
        val br = BufferedReader(isr)

        val line = br.readLine()
        assertEquals("Testing UTF-8 编码: 🚀 Hello World", line)
    }

    @Test
    fun testStringReaderMarkAndReset() {
        val sr = StringReader("abcdef")
        assertTrue(sr.markSupported())

        assertEquals('a'.code, sr.read())
        assertEquals('b'.code, sr.read())

        sr.mark(0)
        assertEquals('c'.code, sr.read())
        assertEquals('d'.code, sr.read())

        sr.reset()
        assertEquals('c'.code, sr.read())
    }
}
