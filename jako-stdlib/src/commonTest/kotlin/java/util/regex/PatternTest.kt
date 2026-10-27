package net.bennyhuo.jako.stdlib.test

import java.util.regex.Pattern
import java.util.regex.Matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternTest {

    @Test
    fun testPatternMatchingAndGroups() {
        val pattern = Pattern.compile("""(\w+)@(\w+\.\w+)""")
        val matcher = pattern.matcher("user@example.com")

        assertTrue(matcher.matches())
        assertEquals("user@example.com", matcher.group(0))
        assertEquals("user", matcher.group(1))
        assertEquals("example.com", matcher.group(2))
    }

    @Test
    fun testCaseInsensitiveFlags() {
        val pattern = Pattern.compile("hello", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher("HeLLo World")

        assertTrue(matcher.find())
        assertEquals("HeLLo", matcher.group())
    }

    @Test
    fun testLookingAtAndFind() {
        val pattern = Pattern.compile("""\d+""")
        val matcher = pattern.matcher("123 456 789")

        assertTrue(matcher.lookingAt())
        assertEquals("123", matcher.group())

        assertTrue(matcher.find())
        assertEquals("456", matcher.group())

        assertTrue(matcher.find())
        assertEquals("789", matcher.group())

        assertFalse(matcher.find())
    }

    @Test
    fun testPatternQuoteAndSpecialEscapes() {
        val rawSpecial = "(a+b*c?)[d]\\E^$"
        val quoted = Pattern.quote(rawSpecial)
        val p = Pattern.compile("(\\w+):" + quoted + ":(\\d+)")
        val m = p.matcher("key:(a+b*c?)[d]\\E^\$:42")
        assertTrue(m.matches())
        assertEquals(2, m.groupCount())
        assertEquals("key", m.group(1))
        assertEquals("42", m.group(2))

        val charClassQuote = Pattern.compile("[\\Q+-*/()\\E]+")
        assertTrue(charClassQuote.matcher("+-*/()").matches())
        assertEquals(0, charClassQuote.matcher("+-*/()").groupCount())

        val literalFlag = Pattern.compile("(a+b)\\E[c]", Pattern.LITERAL)
        assertTrue(literalFlag.matcher("(a+b)\\E[c]").matches())
        assertEquals(0, literalFlag.matcher("(a+b)\\E[c]").groupCount())

        val anchorsAndBellEsc = Pattern.compile("\\A\\a\\ehello\\Z")
        assertTrue(anchorsAndBellEsc.matcher("\u0007\u001Bhello\n").find())
        assertTrue(Pattern.compile("\\Aexact\\z").matcher("exact").matches())
        assertFalse(Pattern.compile("\\Aexact\\z").matcher("exact\n").matches())
    }
}
