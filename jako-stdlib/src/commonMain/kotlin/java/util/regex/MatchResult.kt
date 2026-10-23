@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.regex

/**
 * [Nullability Decision] group(Int) returns String? because optional capturing groups
 * that fail to match return null in Java 8.
 */
expect interface MatchResult {
    fun start(): Int
    fun start(group: Int): Int
    fun end(): Int
    fun end(group: Int): Int
    fun group(): String
    fun group(group: Int): String?
    fun groupCount(): Int
}
