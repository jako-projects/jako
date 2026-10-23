@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.lang.Cloneable
import java.util.Locale

expect abstract class BreakIterator : Cloneable {
    protected constructor()

    open fun clone(): Any
    abstract fun first(): Int
    abstract fun last(): Int
    abstract fun next(n: Int): Int
    abstract fun next(): Int
    abstract fun previous(): Int
    abstract fun following(offset: Int): Int
    abstract fun preceding(offset: Int): Int
    open fun isBoundary(offset: Int): Boolean
    abstract fun current(): Int
    abstract fun getText(): CharacterIterator
    open fun setText(newText: String)
    abstract fun setText(newText: CharacterIterator)

    companion object {
        val DONE: Int
        fun getWordInstance(): BreakIterator
        fun getWordInstance(locale: Locale): BreakIterator
        fun getLineInstance(): BreakIterator
        fun getLineInstance(locale: Locale): BreakIterator
        fun getCharacterInstance(): BreakIterator
        fun getCharacterInstance(locale: Locale): BreakIterator
        fun getSentenceInstance(): BreakIterator
        fun getSentenceInstance(locale: Locale): BreakIterator
        fun getAvailableLocales(): Array<Locale>
    }
}
