package jako.test.io

import java.io.*
import kotlin.test.*

class LineNumberReaderTest {

    @Test
    fun testLineNumberReader() {
        val text = "Line 1\nLine 2\r\nLine 3\rLine 4"
        val lnr = LineNumberReader(StringReader(text))

        assertEquals(0, lnr.lineNumber)
        assertEquals("Line 1", lnr.readLine())
        assertEquals(1, lnr.lineNumber)

        assertEquals("Line 2", lnr.readLine())
        assertEquals(2, lnr.lineNumber)

        assertEquals("Line 3", lnr.readLine())
        assertEquals(3, lnr.lineNumber)

        assertEquals("Line 4", lnr.readLine())
        assertEquals(4, lnr.lineNumber)

        assertNull(lnr.readLine())
        lnr.close()
    }
}
