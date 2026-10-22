@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.lang.Cloneable
import java.util.Locale

expect abstract class Collator : Comparator<Any>, Cloneable {
    protected constructor()

    abstract fun compare(source: String, target: String): Int
    open override fun compare(o1: Any, o2: Any): Int
    abstract fun getCollationKey(source: String): CollationKey
    open fun equals(source: String, target: String): Boolean
    open fun getStrength(): Int
    open fun setStrength(newStrength: Int)
    open fun getDecomposition(): Int
    open fun setDecomposition(decompositionMode: Int)
    open fun clone(): Any

    companion object {
        val PRIMARY: Int
        val SECONDARY: Int
        val TERTIARY: Int
        val IDENTICAL: Int
        val NO_DECOMPOSITION: Int
        val CANONICAL_DECOMPOSITION: Int
        val FULL_DECOMPOSITION: Int

        fun getInstance(): Collator
        fun getInstance(desiredLocale: Locale): Collator
        fun getAvailableLocales(): Array<Locale>
    }
}
