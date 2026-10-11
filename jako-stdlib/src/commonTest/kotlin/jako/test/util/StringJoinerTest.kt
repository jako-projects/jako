package jako.test.util

import java.util.StringJoiner
import kotlin.test.Test
import kotlin.test.assertEquals

class StringJoinerTest {

    @Test
    fun testBasicJoiner() {
        val sj = StringJoiner(", ")
        sj.add("a").add("b").add("c")
        assertEquals("a, b, c", sj.toString())
        assertEquals(7, sj.length())
    }

    @Test
    fun testPrefixAndSuffix() {
        val sj = StringJoiner(", ", "[", "]")
        sj.add("1").add("2")
        assertEquals("[1, 2]", sj.toString())
        assertEquals(6, sj.length())
    }

    @Test
    fun testEmptyValue() {
        val sj = StringJoiner(", ", "[", "]")
        assertEquals("[]", sj.toString())

        val sj2 = StringJoiner(", ")
        sj2.setEmptyValue("EMPTY")
        assertEquals("EMPTY", sj2.toString())
    }

    @Test
    fun testMerge() {
        val sj1 = StringJoiner(", ", "[", "]")
        sj1.add("a").add("b")

        val sj2 = StringJoiner(", ", "{", "}")
        sj2.add("c").add("d")

        sj1.merge(sj2)
        assertEquals("[a, b, c, d]", sj1.toString())
    }
}
