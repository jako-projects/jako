@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.regex

import java.io.Serializable

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
