package jako.android.test.text

import android.text.TextUtils
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextUtilsTest {

    @Test
    fun testIsEmptyAndEquals() {
        assertTrue(TextUtils.isEmpty(null))
        assertTrue(TextUtils.isEmpty(""))
        assertFalse(TextUtils.isEmpty(" "))
        assertFalse(TextUtils.isEmpty("abc"))

        assertTrue(TextUtils.equals(null, null))
        assertFalse(TextUtils.equals(null, ""))
        assertTrue(TextUtils.equals("hello", StringBuilder("hello")))
        assertFalse(TextUtils.equals("hello", "world"))
    }

    @Test
    fun testIsDigitsOnlyAndTrimmedLength() {
        assertTrue(TextUtils.isDigitsOnly(""))
        assertTrue(TextUtils.isDigitsOnly("0123456789"))
        assertFalse(TextUtils.isDigitsOnly("123a45"))
        assertFalse(TextUtils.isDigitsOnly("-123"))

        assertEquals(0, TextUtils.getTrimmedLength("   \t\n "))
        assertEquals(5, TextUtils.getTrimmedLength("  hello \n "))
        assertEquals(5, TextUtils.getTrimmedLength(" a b c "))
    }

    @Test
    fun testJoinAndSplit() {
        assertEquals("a,b,c", TextUtils.join(",", arrayOf("a", "b", "c")))
        assertEquals("1-2-3", TextUtils.join("-", listOf(1, 2, 3)))
        assertEquals("", TextUtils.join(",", emptyList<String>()))

        assertContentEquals(emptyArray(), TextUtils.split("", ","))
        assertContentEquals(arrayOf("a", "b", ""), TextUtils.split("a,b,", ","))
        assertContentEquals(arrayOf("a", "b", "c"), TextUtils.split("a:b:c", Pattern.compile(":")))
    }

    @Test
    fun testHtmlEncodeAndSearch() {
        assertEquals(
            "&lt;div class=&quot;test&quot;&gt;Tom &amp; &#39;Jerry&#39;&lt;/div&gt;",
            TextUtils.htmlEncode("<div class=\"test\">Tom & 'Jerry'</div>")
        )

        val cs: CharSequence = StringBuilder("abca_abc_d")
        assertEquals(0, TextUtils.indexOf(cs, 'a'))
        assertEquals(3, TextUtils.indexOf(cs, 'a', 1))
        assertEquals(-1, TextUtils.indexOf(cs, 'd', 0, 5))
        assertEquals(5, TextUtils.indexOf(cs, "abc", 1))
        assertEquals(5, TextUtils.lastIndexOf(cs, 'a'))
        assertEquals(3, TextUtils.lastIndexOf(cs, 'a', 4))
        assertEquals("abc", TextUtils.substring(cs, 0, 3))
        assertTrue(TextUtils.regionMatches(cs, 0, "abc", 0, 3))
        assertFalse(TextUtils.regionMatches(cs, 1, "abc", 0, 3))
    }
}
