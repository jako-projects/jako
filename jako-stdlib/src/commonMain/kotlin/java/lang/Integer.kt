@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Int

expect object Integer {
    val MAX_VALUE: Int
    val MIN_VALUE: Int
    fun parseInt(s: String): Int
    fun parseInt(s: String, radix: Int): Int
    fun toHexString(i: Int): String
    fun toBinaryString(i: Int): String
    fun compare(x: Int, y: Int): Int
    fun numberOfLeadingZeros(i: Int): Int
    fun valueOf(i: Int): Int
    fun valueOf(s: String): Int
    fun valueOf(s: String, radix: Int): Int
}
