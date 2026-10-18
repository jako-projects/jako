@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.io.Serializable
import java.lang.Cloneable
import java.util.Locale

expect open class DecimalFormatSymbols : Cloneable, Serializable {
    constructor()
    constructor(locale: Locale)

    open fun getZeroDigit(): Char
    open fun setZeroDigit(zeroDigit: Char)
    open fun getGroupingSeparator(): Char
    open fun setGroupingSeparator(groupingSeparator: Char)
    open fun getDecimalSeparator(): Char
    open fun setDecimalSeparator(decimalSeparator: Char)
    open fun getPerMill(): Char
    open fun setPerMill(perMill: Char)
    open fun getPercent(): Char
    open fun setPercent(percent: Char)
    open fun getDigit(): Char
    open fun setDigit(digit: Char)
    open fun getPatternSeparator(): Char
    open fun setPatternSeparator(patternSeparator: Char)
    open fun getInfinity(): String
    open fun setInfinity(infinity: String)
    open fun getNaN(): String
    open fun setNaN(NaN: String)
    open fun getMinusSign(): Char
    open fun setMinusSign(minusSign: Char)
    open fun getCurrencySymbol(): String
    open fun setCurrencySymbol(currency: String)
    open fun getInternationalCurrencySymbol(): String
    open fun setInternationalCurrencySymbol(currencyCode: String)
    open fun getMonetaryDecimalSeparator(): Char
    open fun setMonetaryDecimalSeparator(sep: Char)
    open fun getExponentSeparator(): String
    open fun setExponentSeparator(exp: String)
    open fun clone(): Any

    companion object {
        fun getAvailableLocales(): Array<Locale>
        fun getInstance(): DecimalFormatSymbols
        fun getInstance(locale: Locale): DecimalFormatSymbols
    }
}

expect open class DateFormatSymbols : Serializable, Cloneable {
    constructor()
    constructor(locale: Locale)

    open fun getEras(): Array<String>
    open fun setEras(newEras: Array<String>)
    open fun getMonths(): Array<String>
    open fun setMonths(newMonths: Array<String>)
    open fun getShortMonths(): Array<String>
    open fun setShortMonths(newShortMonths: Array<String>)
    open fun getWeekdays(): Array<String>
    open fun setWeekdays(newWeekdays: Array<String>)
    open fun getShortWeekdays(): Array<String>
    open fun setShortWeekdays(newShortWeekdays: Array<String>)
    open fun getAmPmStrings(): Array<String>
    open fun setAmPmStrings(newAmpms: Array<String>)
    open fun getZoneStrings(): Array<Array<String>>
    open fun setZoneStrings(newZoneStrings: Array<Array<String>>)
    open fun getLocalPatternChars(): String
    open fun setLocalPatternChars(newLocalPatternChars: String)
    open fun clone(): Any

    companion object {
        fun getAvailableLocales(): Array<Locale>
        fun getInstance(): DateFormatSymbols
        fun getInstance(locale: Locale): DateFormatSymbols
    }
}

expect interface CharacterIterator : Cloneable {
    fun first(): Char
    fun last(): Char
    fun current(): Char
    fun next(): Char
    fun previous(): Char
    fun setIndex(position: Int): Char
    fun getBeginIndex(): Int
    fun getEndIndex(): Int
    fun getIndex(): Int
    fun clone(): Any

    companion object {
        val DONE: Char
    }
}

expect class StringCharacterIterator : CharacterIterator {
    constructor(text: String)
    constructor(text: String, pos: Int)
    constructor(text: String, begin: Int, end: Int, pos: Int)

    fun setText(text: String)
    override fun first(): Char
    override fun last(): Char
    override fun setIndex(p: Int): Char
    override fun current(): Char
    override fun next(): Char
    override fun previous(): Char
    override fun getBeginIndex(): Int
    override fun getEndIndex(): Int
    override fun getIndex(): Int
    override fun clone(): Any
}

expect abstract class CollationKey : Comparable<CollationKey> {
    protected constructor(source: String?)
    open fun getSourceString(): String
    abstract override fun compareTo(other: CollationKey): Int
    abstract fun toByteArray(): ByteArray
}

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

expect open class ChoiceFormat : NumberFormat {
    constructor(newPattern: String)
    constructor(limits: DoubleArray, formats: Array<String>)

    open fun applyPattern(newPattern: String)
    open fun toPattern(): String
    open fun setChoices(limits: DoubleArray, formats: Array<String>)
    open fun getLimits(): DoubleArray
    open fun getFormats(): Array<Any>
    override fun format(number: Long, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder
    override fun format(number: Double, toAppendTo: StringBuilder, status: FieldPosition): StringBuilder
    override fun parse(text: String, status: ParsePosition): Number?

    companion object {
        fun nextDouble(d: Double): Double
        fun previousDouble(d: Double): Double
        fun nextDouble(d: Double, positive: Boolean): Double
    }
}

expect class Bidi {
    constructor(paragraph: String, flags: Int)
    constructor(
        text: CharArray,
        textStart: Int,
        embeddings: ByteArray?,
        embStart: Int,
        paragraphLength: Int,
        flags: Int
    )

    fun createLineBidi(lineStart: Int, lineLimit: Int): Bidi
    fun isMixed(): Boolean
    fun isLeftToRight(): Boolean
    fun isRightToLeft(): Boolean
    fun getLength(): Int
    fun baseIsLeftToRight(): Boolean
    fun getBaseLevel(): Int
    fun getLevelAt(offset: Int): Int
    fun getRunCount(): Int
    fun getRunLevel(run: Int): Int
    fun getRunStart(run: Int): Int
    fun getRunLimit(run: Int): Int

    companion object {
        val DIRECTION_LEFT_TO_RIGHT: Int
        val DIRECTION_RIGHT_TO_LEFT: Int
        val DIRECTION_DEFAULT_LEFT_TO_RIGHT: Int
        val DIRECTION_DEFAULT_RIGHT_TO_LEFT: Int

        fun requiresBidi(text: CharArray, start: Int, limit: Int): Boolean
        fun reorderVisually(
            levels: ByteArray,
            levelStart: Int,
            objects: Array<Any?>,
            objectStart: Int,
            count: Int
        )
    }
}
