package jako.test.text

import java.text.MessageFormat
import java.text.ParseException
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class MessageFormatTest {

    @Test
    fun testStaticFormatHelloWorld() {
        assertEquals("Hello World", MessageFormat.format("Hello {0}", "World"))
    }

    @Test
    fun testStaticFormatTwoArguments() {
        assertEquals("a b", MessageFormat.format("{0} {1}", "a", "b"))
    }

    @Test
    fun testInstanceFormat() {
        val format = MessageFormat("Hello {0}", Locale.US)
        assertEquals("Hello World", format.format(arrayOf("World")))
        assertEquals("Hello {0}", format.toPattern())
    }

    @Test
    fun testMissingArgumentLeavesPlaceholder() {
        assertEquals("Hello {0}", MessageFormat.format("Hello {0}"))
        assertEquals("a {1}", MessageFormat.format("{0} {1}", "a"))
        assertEquals("Hello null", MessageFormat.format("Hello {0}", null as Any?))
    }

    @Test
    fun testNumberAndChoiceSubformatsAndLocales() {
        val usFmt = MessageFormat("Price: {0,number,#,##0.00} ({1,number,percent})", Locale.US)
        assertEquals("Price: 1,234.50 (25%)", usFmt.format(arrayOf<Any?>(1234.5, 0.25)))

        val deFmt = MessageFormat("Price: {0,number,#,##0.00}", Locale.GERMANY)
        assertEquals("Price: 1.234,50", deFmt.format(arrayOf<Any?>(1234.5)))

        val choicePattern = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}."
        val choiceFmt = MessageFormat(choicePattern, Locale.US)
        assertEquals("There are no files.", choiceFmt.format(arrayOf<Any?>(0)))
        assertEquals("There is one file.", choiceFmt.format(arrayOf<Any?>(1)))
        assertEquals("There are 5 files.", choiceFmt.format(arrayOf<Any?>(5)))
    }

    @Test
    fun testQuoteEscapingAndParsing() {
        val quoted = MessageFormat("It''s '{0}' = {0}", Locale.US)
        assertEquals("It's {0} = 42", quoted.format(arrayOf<Any?>(42)))

        val parser = MessageFormat("User {0} has {1,number,integer} items", Locale.US)
        val parsed = parser.parse("User Alice has 15 items")
        assertNotNull(parsed)
        assertEquals(2, parsed.size)
        assertEquals("Alice", parsed[0])
        assertEquals(15L, (parsed[1] as Number).toLong())

        assertFailsWith<ParseException> {
            parser.parse("Unmatched text")
        }
        assertFailsWith<IllegalArgumentException> {
            MessageFormat("Unclosed {0")
        }
        assertFailsWith<IllegalArgumentException> {
            MessageFormat("Invalid {-1}")
        }
    }

    @Test
    fun testNestedChoiceWithHashPatternAndQuotedPipes() {
        val nestedFmt = MessageFormat(
            "{0,choice,0#no items|1#one ''item''|1<'{0,number,#,##0.00}' items}",
            Locale.US
        )
        assertEquals("no items", nestedFmt.format(arrayOf<Any?>(0)))
        assertEquals("one 'item'", nestedFmt.format(arrayOf<Any?>(1)))
        assertEquals("1,234.50 items", nestedFmt.format(arrayOf<Any?>(1234.5)))

        val cf = java.text.ChoiceFormat("0#'#0' items|1#one '|' item|1<many items")
        assertEquals("#0 items", cf.format(0.0))
        assertEquals("one | item", cf.format(1.0))
        assertEquals("many items", cf.format(2.0))

        // Unquoted '#' in a ChoiceFormat format segment is invalid in JDK 8
        assertFailsWith<IllegalArgumentException> {
            java.text.ChoiceFormat("0#zero|1<unquoted # hash")
        }
    }
}


