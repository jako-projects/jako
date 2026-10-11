package jako.test.text

import java.text.MessageFormat
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val format = MessageFormat("Hello {0}")
        assertEquals("Hello World", format.format(arrayOf("World")))
        assertEquals("Hello {0}", format.toPattern())
    }

    @Test
    fun testMissingArgumentLeavesPlaceholder() {
        assertEquals("Hello {0}", MessageFormat.format("Hello {0}"))
        assertEquals("a {1}", MessageFormat.format("{0} {1}", "a"))
        assertEquals("Hello null", MessageFormat.format("Hello {0}", null as Any?))
    }
}
