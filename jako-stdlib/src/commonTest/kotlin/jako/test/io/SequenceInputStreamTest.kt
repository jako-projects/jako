package jako.test.io

import java.io.*
import kotlin.test.*

class SequenceInputStreamTest {

    @Test
    fun testSequenceInputStreamTwoStreams() {
        val s1 = ByteArrayInputStream(byteArrayOf(1, 2, 3))
        val s2 = ByteArrayInputStream(byteArrayOf(4, 5, 6))
        val seq = SequenceInputStream(s1, s2)

        val out = ByteArrayOutputStream()
        val buf = ByteArray(4)
        var n: Int
        while (seq.read(buf).also { n = it } != -1) {
            out.write(buf, 0, n)
        }

        val result = out.toByteArray()
        assertEquals(6, result.size)
        assertEquals(1, result[0].toInt())
        assertEquals(2, result[1].toInt())
        assertEquals(3, result[2].toInt())
        assertEquals(4, result[3].toInt())
        assertEquals(5, result[4].toInt())
        assertEquals(6, result[5].toInt())

        seq.close()
    }
}
