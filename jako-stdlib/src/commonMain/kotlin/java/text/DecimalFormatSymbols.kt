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
