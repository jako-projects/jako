package jako.test.util

import java.util.Formatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormatterTest {

    @Test
    fun testFormatStringAndDecimal() {
        val formatter = Formatter()
        formatter.format("%s-%d", "a", 1)
        assertEquals("a-1", formatter.toString())
        formatter.close()
    }

    @Test
    fun testFormatExtrasAndAppendable() {
        val dest = StringBuilder()
        val formatter = Formatter(dest)
        formatter.format("%s %x %f%%", "ok", 255, 1.5)
        val text = dest.toString()
        assertTrue(
            text == "ok ff 1.500000%" || text == "ok ff 1,500000%",
            "unexpected formatter output: $text"
        )
        formatter.close()
    }

    @Test
    fun testFormatNewlineAndPercent() {
        val formatter = Formatter()
        formatter.format("a%%b%nc")
        val text = formatter.toString()
        assertTrue(text.startsWith("a%b"))
        assertTrue(text.endsWith("c"))
        assertTrue(text.contains("\n") || text.contains("\r\n"))
        formatter.close()
    }

    @Test
    fun testFormatWithPercentInArgument() {
        val formatter = Formatter()
        formatter.format("Parsing: <%s> against <%s>", "http://%30%78", "http://other.com/")
        assertEquals("Parsing: <http://%30%78> against <http://other.com/>", formatter.toString())
        formatter.close()
    }

    @Test
    fun testFormatterLocale() {
        val formatter = Formatter(java.util.Locale.US)
        assertEquals(java.util.Locale.US, formatter.locale())
        formatter.format(java.util.Locale.US, "hello %s", "world")
        assertEquals("hello world", formatter.toString())
        formatter.close()
    }

    @Test
    fun testZeroPadding() {
        val formatter = Formatter()
        formatter.format("%08x %05d", 0, 42)
        assertEquals("00000000 00042", formatter.toString())
        formatter.close()
    }

    @Test
    fun testStringFormatExtensions() {
        assertEquals("Hello Alice, age 30", "Hello %s, age %d".format("Alice", 30))
        assertEquals("Hello Bob, score 99", "Hello %s, score %d".format(java.util.Locale.US, "Bob", 99))
        assertEquals("Hello Alice, age 30", String.format("Hello %s, age %d", "Alice", 30))
        assertEquals("Hello Bob, score 99", String.format(java.util.Locale.US, "Hello %s, score %d", "Bob", 99))
    }
}
