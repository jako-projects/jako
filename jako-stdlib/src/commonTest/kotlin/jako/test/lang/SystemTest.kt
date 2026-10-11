package jako.test.lang

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.System
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemTest {

    @Test
    fun testLineSeparatorAndProperties() {
        val sep = System.lineSeparator()
        assertTrue(sep.isNotEmpty())
        assertEquals(System.getProperty("line.separator"), sep)
        assertTrue(!System.getProperty("file.separator").isNullOrEmpty())
        assertTrue(!System.getProperty("path.separator").isNullOrEmpty())
    }

    @Test
    fun testSetOutAndSetIn() {
        val captured = ByteArrayOutputStream()
        val previousOut = System.out
        try {
            System.setOut(PrintStream(captured))
            System.out.print("hello")
            System.out.flush()
            assertEquals("hello", captured.toByteArray().decodeToString())
        } finally {
            System.setOut(previousOut)
        }

        val previousIn = System.`in`
        try {
            System.setIn(ByteArrayInputStream("ab".encodeToByteArray()))
            assertEquals('a'.code, System.`in`.read())
            assertEquals('b'.code, System.`in`.read())
            assertEquals(-1, System.`in`.read())
        } finally {
            System.setIn(previousIn)
        }
    }
}
