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

    @Test
    fun testReaderFilterReaderAndFilterWriter() {
        val customReader: Reader = object : Reader() {
            private val text = "hello"
            private var pos = 0
            override fun read(cbuf: CharArray, off: Int, len: Int): Int {
                if (pos >= text.length) return -1
                val n = minOf(len, text.length - pos)
                for (i in 0 until n) {
                    cbuf[off + i] = text[pos + i]
                }
                pos += n
                return n
            }
            override fun close() {}
        }
        assertFalse(customReader.markSupported())
        assertFailsWith<IOException> { customReader.reset() }
        assertEquals('h'.code, customReader.read())
        assertEquals(1L, customReader.skip(1L))
        val targetBuf = java.nio.CharBuffer.allocate(3)
        assertEquals(3, customReader.read(targetBuf))
        targetBuf.flip()
        assertEquals("llo", targetBuf.toString())
        customReader.close()

        class UppercaseFilterReader(inReader: Reader) : FilterReader(inReader) {
            override fun read(): Int {
                val c = super.read()
                return if (c == -1) -1 else c.toChar().uppercaseChar().code
            }
        }
        val fr = UppercaseFilterReader(StringReader("abc"))
        assertTrue(fr.markSupported())
        assertEquals('A'.code, fr.read())
        fr.mark(10)
        val cbuf = CharArray(2)
        assertEquals(2, fr.read(cbuf, 0, 2))
        assertEquals("bc", cbuf.concatToString())
        fr.reset()
        assertEquals(1L, fr.skip(1L))
        assertEquals('C'.code, fr.read())
        fr.close()

        val sw = StringWriter()
        class PassThroughFilterWriter(out: Writer) : FilterWriter(out)
        val fw = PassThroughFilterWriter(sw)
        fw.write('X'.code)
        fw.write(charArrayOf('Y', 'Z', '!'), 0, 2)
        fw.write("-end-extra", 0, 4)
        fw.flush()
        fw.close()
        assertEquals("XYZ-end", sw.toString())
    }
}
