@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.text

import java.util.regex.Pattern

/**
 * Android `android.text.TextUtils` API declaration for Kotlin Multiplatform.
 *
 * [Nullability Decision]:
 * - `isEmpty(str: CharSequence?)` and `equals(a: CharSequence?, b: CharSequence?)` accept nullable `CharSequence?`.
 * - Other string/sequence processing functions take non-null parameters and return non-null `String` / `Array<String>`.
 */
expect class TextUtils {
    companion object {
        fun isEmpty(str: CharSequence?): Boolean
        fun equals(a: CharSequence?, b: CharSequence?): Boolean
        fun isDigitsOnly(str: CharSequence): Boolean
        fun getTrimmedLength(s: CharSequence): Int
        fun join(delimiter: CharSequence, tokens: Array<*>): String
        fun join(delimiter: CharSequence, tokens: Iterable<*>): String
        fun split(text: String, expression: String): Array<String>
        fun split(text: String, pattern: Pattern): Array<String>
        fun htmlEncode(s: String): String
        fun indexOf(s: CharSequence, ch: Char): Int
        fun indexOf(s: CharSequence, ch: Char, start: Int): Int
        fun indexOf(s: CharSequence, ch: Char, start: Int, end: Int): Int
        fun indexOf(s: CharSequence, needle: CharSequence): Int
        fun indexOf(s: CharSequence, needle: CharSequence, start: Int): Int
        fun indexOf(s: CharSequence, needle: CharSequence, start: Int, end: Int): Int
        fun lastIndexOf(s: CharSequence, ch: Char): Int
        fun lastIndexOf(s: CharSequence, ch: Char, last: Int): Int
        fun lastIndexOf(s: CharSequence, ch: Char, start: Int, last: Int): Int
        fun substring(source: CharSequence, start: Int, end: Int): String
        fun regionMatches(one: CharSequence, toffset: Int, two: CharSequence, ooffset: Int, len: Int): Boolean
    }
}
