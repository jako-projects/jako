package jako.test.util

import java.util.StringTokenizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringTokenizerTest {

    @Test
    fun testDefaultDelimiters() {
        val st = StringTokenizer("Hello World Jako KMP")
        assertEquals(4, st.countTokens())
        assertTrue(st.hasMoreTokens())
        assertEquals("Hello", st.nextToken())
        assertEquals("World", st.nextToken())
        assertEquals("Jako", st.nextToken())
        assertEquals("KMP", st.nextToken())
        assertFalse(st.hasMoreTokens())
    }

    @Test
    fun testCustomDelimiters() {
        val st = StringTokenizer("a,b;c:d", ",;:")
        assertEquals(4, st.countTokens())
        val tokens = mutableListOf<String>()
        while (st.hasMoreElements()) {
            tokens.add(st.nextElement() as String)
        }
        assertEquals(listOf("a", "b", "c", "d"), tokens)
    }

    @Test
    fun testReturnDelims() {
        val st = StringTokenizer("a+b*c", "+*", true)
        assertEquals(5, st.countTokens())
        assertEquals("a", st.nextToken())
        assertEquals("+", st.nextToken())
        assertEquals("b", st.nextToken())
        assertEquals("*", st.nextToken())
        assertEquals("c", st.nextToken())
        assertFalse(st.hasMoreTokens())
    }
}
