@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Int

expect object Character {
    fun isISOControl(codePoint: Int): Boolean
    fun isISOControl(ch: Char): Boolean
    fun isDigit(ch: Char): Boolean
    fun isDigit(codePoint: Int): Boolean
    fun isLetter(ch: Char): Boolean
    fun isLetter(codePoint: Int): Boolean
    fun isLetterOrDigit(ch: Char): Boolean
    fun isLetterOrDigit(codePoint: Int): Boolean
    fun isWhitespace(ch: Char): Boolean
    fun isWhitespace(codePoint: Int): Boolean
    fun toLowerCase(ch: Char): Char
    fun toLowerCase(codePoint: Int): Int
    fun toUpperCase(ch: Char): Char
    fun toUpperCase(codePoint: Int): Int
    fun digit(ch: Char, radix: Int): Int
    fun digit(codePoint: Int, radix: Int): Int
    fun forDigit(digit: Int, radix: Int): Char
    fun charCount(codePoint: Int): Int
    fun toChars(codePoint: Int): CharArray
    fun codePointAt(seq: CharSequence, index: Int): Int
}
