package jako.test.util

import java.util.Formatter
import java.util.FormatterClosedException
import java.util.IllegalFormatConversionException
import java.util.IllegalFormatException
import java.util.Locale
import java.util.MissingFormatArgumentException
import java.util.UnknownFormatConversionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun testNullString() {
        val formatter = Formatter(Locale.US)
        formatter.format("%s", null as Any?)
        assertEquals("null", formatter.toString())
        formatter.close()
    }

    @Test
    fun testSignedDecimalAndHex() {
        val formatter = Formatter(Locale.US)
        formatter.format("%d %08d %x %X", -1, -42, -1, 255)
        assertEquals("-1 -0000042 ffffffff FF", formatter.toString())
        formatter.close()
    }

    @Test
    fun testUppercasePrecisionAndArgumentIndex() {
        val formatter = Formatter(Locale.US)
        formatter.format("%S %.3s %1\$s %<s", "ab", "abcdef", "z")
        assertEquals("AB abc ab ab", formatter.toString())
        formatter.close()
    }

    @Test
    fun testWidthAndJustification() {
        val formatter = Formatter(Locale.US)
        formatter.format("[%5d][%-5d]", 7, 7)
        assertEquals("[    7][7    ]", formatter.toString())
        formatter.close()
    }

    @Test
    fun testFloatRoundingAndSpecials() {
        val formatter = Formatter(Locale.US)
        formatter.format(
            "%f|%.0f|%.0f|%f|%f|%f",
            0.0,
            1.5,
            2.5,
            -1.25,
            Double.NaN,
            Double.POSITIVE_INFINITY,
        )
        assertEquals("0.000000|2|3|-1.250000|NaN|Infinity", formatter.toString())
        formatter.close()

        val negativeInfinity = Formatter(Locale.US)
        negativeInfinity.format("%f", Double.NEGATIVE_INFINITY)
        assertEquals("-Infinity", negativeInfinity.toString())
        negativeInfinity.close()
    }

    @Test
    fun testGrouping() {
        val formatter = Formatter(Locale.US)
        formatter.format("%,d %,d", 1234, -1234)
        assertEquals("1,234 -1,234", formatter.toString())
        formatter.close()
    }

    @Test
    fun testPlusAndParentheses() {
        val formatter = Formatter(Locale.US)
        formatter.format("%+d %(d", 7, -42)
        assertEquals("+7 (42)", formatter.toString())
        formatter.close()
    }

    @Test
    fun testGermanyDecimalSeparator() {
        val formatter = Formatter(Locale.GERMANY)
        formatter.format("%f", 1.5)
        assertEquals("1,500000", formatter.toString())
        formatter.close()
    }

    @Test
    fun testFormatExceptions() {
        val unknown = Formatter(Locale.US)
        assertFailsWith<UnknownFormatConversionException> { unknown.format("%q") }
        unknown.close()

        val trailing = Formatter(Locale.US)
        assertFailsWith<UnknownFormatConversionException> { trailing.format("%") }
        trailing.close()

        val badConversion = Formatter(Locale.US)
        assertFailsWith<IllegalFormatConversionException> { badConversion.format("%d", "x") }
        badConversion.close()

        val missing = Formatter(Locale.US)
        assertFailsWith<MissingFormatArgumentException> { missing.format("%d") }
        missing.close()

        val previous = Formatter(Locale.US)
        assertFailsWith<MissingFormatArgumentException> { previous.format("%<s") }
        previous.close()

        val zeroIndex = Formatter(Locale.US)
        assertFailsWith<IllegalFormatException> { zeroIndex.format("%0\$s", "x") }
        zeroIndex.close()
    }

    @Test
    fun testClosedFormatter() {
        val formatter = Formatter(Locale.US)
        formatter.close()
        assertFailsWith<FormatterClosedException> { formatter.format("%s", "closed") }
        assertFailsWith<FormatterClosedException> { formatter.toString() }
    }
}
