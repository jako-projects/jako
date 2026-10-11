package jako.test.io

import java.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IOExtensionsTest {

    @Test
    fun testInputStreamCopyTo() {
        val inputData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val inStream = ByteArrayInputStream(inputData)
        val outStream = ByteArrayOutputStream()

        val copied = inStream.copyTo(outStream, bufferSize = 4)
        assertEquals(10L, copied)
        val result = outStream.toByteArray()
        assertEquals(10, result.size)
        for (i in 0 until 10) {
            assertEquals(inputData[i], result[i])
        }
    }

    @Test
    fun testInputStreamReaderAndBufferedReader() {
        val text = "Hello Kotlin Extensions\nLine 2"
        val bytes = text.encodeToByteArray()
        val inStream = ByteArrayInputStream(bytes)

        val reader = inStream.reader()
        val bufferedReader = inStream.bufferedReader()

        val inStream1 = ByteArrayInputStream(bytes)
        val textRead = inStream1.bufferedReader().readText()
        assertEquals(text, textRead)
    }

    @Test
    fun testOutputStreamWriterAndBufferedWriter() {
        val outStream = ByteArrayOutputStream()
        val writer = outStream.writer()
        writer.write("Hello Output")
        writer.flush()

        assertEquals("Hello Output", outStream.toByteArray().decodeToString())

        val outStream2 = ByteArrayOutputStream()
        val bufferedWriter = outStream2.bufferedWriter()
        bufferedWriter.write("Line 1\nLine 2")
        bufferedWriter.flush()

        assertEquals("Line 1\nLine 2", outStream2.toByteArray().decodeToString())
    }

    @Test
    fun testReaderExtensions() {
        val content = "Alpha\nBeta\nGamma"
        val reader = StringReader(content)

        val lines = reader.readLines()
        assertEquals(3, lines.size)
        assertEquals("Alpha", lines[0])
        assertEquals("Beta", lines[1])
        assertEquals("Gamma", lines[2])

        val reader2 = StringReader(content)
        val collected = mutableListOf<String>()
        reader2.forEachLine { collected.add(it) }
        assertEquals(3, collected.size)
        assertEquals("Alpha", collected[0])

        val reader3 = StringReader(content)
        val joined = reader3.useLines { seq ->
            seq.joinToString(",")
        }
        assertEquals("Alpha,Beta,Gamma", joined)

        val reader4 = StringReader("Hello Writer Copy")
        val writer = StringWriter()
        val copied = reader4.copyTo(writer, bufferSize = 4)
        assertEquals(17L, copied)
        assertEquals("Hello Writer Copy", writer.toString())
    }

    @Test
    fun testBufferedExtensions() {
        val sr = StringReader("Test")
        val br = sr.buffered()
        assertTrue(br is BufferedReader)
        assertEquals("Test", br.readLine())

        val sw = StringWriter()
        val bw = sw.buffered()
        assertTrue(bw is BufferedWriter)
        bw.write("Test Writer")
        bw.flush()
        assertEquals("Test Writer", sw.toString())
    }
}
