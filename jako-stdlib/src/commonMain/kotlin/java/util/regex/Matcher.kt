@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.regex

expect class Matcher : MatchResult {
    fun pattern(): Pattern
    fun toMatchResult(): MatchResult
    fun reset(): Matcher
    fun reset(input: CharSequence): Matcher
    fun region(start: Int, end: Int): Matcher
    fun regionStart(): Int
    fun regionEnd(): Int
    fun usePattern(newPattern: Pattern): Matcher
    fun matches(): Boolean
    fun lookingAt(): Boolean
    fun find(): Boolean
    fun find(start: Int): Boolean
    override fun group(): String
    override fun group(group: Int): String?
    fun group(name: String): String?
    override fun groupCount(): Int
    override fun start(): Int
    override fun start(group: Int): Int
    override fun end(): Int
    override fun end(group: Int): Int
    fun replaceAll(replacement: String): String
    fun replaceFirst(replacement: String): String

    companion object {
        fun quoteReplacement(s: String): String
    }
}
