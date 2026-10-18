@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.regex

import java.io.Serializable

expect class Matcher {
    var input: CharSequence
    fun reset(input: CharSequence): Matcher
    fun region(start: Int, end: Int): Matcher
    fun usePattern(newPattern: Pattern): Matcher
    fun matches(): Boolean
    fun lookingAt(): Boolean
    fun find(): Boolean
    fun find(start: Int): Boolean
    fun group(): String
    fun group(index: Int): String
    fun groupCount(): Int
    fun start(): Int
    fun start(group: Int): Int
    fun end(): Int
    fun replaceAll(replacement: String): String
    fun replaceFirst(replacement: String): String
}

expect class Pattern : Serializable {
    fun pattern(): String
    fun flags(): Int
    fun matcher(input: CharSequence): Matcher
    fun split(input: CharSequence): Array<String>
    fun split(input: CharSequence, limit: Int): Array<String>

    companion object {
        val UNIX_LINES: Int
        val CASE_INSENSITIVE: Int
        val COMMENTS: Int
        val MULTILINE: Int
        val LITERAL: Int
        val DOTALL: Int
        val UNICODE_CASE: Int
        val CANON_EQ: Int
        val UNICODE_CHARACTER_CLASS: Int

        fun compile(pattern: String): Pattern
        fun compile(pattern: String, flags: Int): Pattern
        fun matches(regex: String, input: CharSequence): Boolean
        fun quote(s: String): String
    }
}
