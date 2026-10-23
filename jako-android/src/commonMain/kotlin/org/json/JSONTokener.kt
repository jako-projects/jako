@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.json

/**
 * [Nullability Decision] JSONTokener(inString: String?) accepts nullable String in AOSP.
 */
expect open class JSONTokener {
    constructor(inString: String?)

    open fun nextValue(): Any
    open fun nextString(quote: Char): String
    open fun syntaxError(message: String): JSONException
    open fun more(): Boolean
    open fun next(): Char
    open fun next(c: Char): Char
    open fun next(length: Int): String
    open fun nextClean(): Char
    open fun nextTo(excluded: String): String
    open fun nextTo(excluded: Char): String
    open fun skipPast(thru: String)
    open fun skipTo(to: Char): Char
    open fun back()

    companion object {
        fun dehexchar(hex: Char): Int
    }
}
