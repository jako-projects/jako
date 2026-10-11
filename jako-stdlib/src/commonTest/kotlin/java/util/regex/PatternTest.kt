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
}
