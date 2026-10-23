package java.text

import java.io.Serializable
import java.lang.Cloneable
import java.util.Locale

actual open class DecimalFormatSymbols : Cloneable, Serializable {
    private var zeroDigit: Char = '0'
    private var groupingSeparator: Char = ','
    private var decimalSeparator: Char = '.'
    private var perMill: Char = '\u2030'
    private var percent: Char = '%'
    private var digit: Char = '#'
    private var patternSeparator: Char = ';'
    private var infinity: String = "\u221E"
    private var nan: String = "NaN"
    private var minusSign: Char = '-'
    private var currencySymbol: String = "$"
    private var intlCurrencySymbol: String = "USD"
    private var monetarySeparator: Char = '.'
    private var exponentSeparator: String = "E"

    actual constructor() : this(Locale.getDefault())

    actual constructor(locale: Locale) {
        when (locale.getLanguage()) {
            "de", "fr", "es", "it" -> {
                decimalSeparator = ','
                monetarySeparator = ','
                groupingSeparator = if (locale.getLanguage() == "fr") '\u00A0' else '.'
                currencySymbol = "€"
                intlCurrencySymbol = "EUR"
            }
            "zh" -> {
                currencySymbol = "￥"
                intlCurrencySymbol = "CNY"
            }
            "ja" -> {
                currencySymbol = "￥"
                intlCurrencySymbol = "JPY"
            }
            "en" -> {
                if (locale.getCountry() == "GB") {
                    currencySymbol = "£"
                    intlCurrencySymbol = "GBP"
                }
            }
        }
    }

    actual open fun getZeroDigit(): Char = zeroDigit
    actual open fun setZeroDigit(zeroDigit: Char) { this.zeroDigit = zeroDigit }

    actual open fun getGroupingSeparator(): Char = groupingSeparator
    actual open fun setGroupingSeparator(groupingSeparator: Char) { this.groupingSeparator = groupingSeparator }

    actual open fun getDecimalSeparator(): Char = decimalSeparator
    actual open fun setDecimalSeparator(decimalSeparator: Char) { this.decimalSeparator = decimalSeparator }

    actual open fun getPerMill(): Char = perMill
    actual open fun setPerMill(perMill: Char) { this.perMill = perMill }

    actual open fun getPercent(): Char = percent
    actual open fun setPercent(percent: Char) { this.percent = percent }

    actual open fun getDigit(): Char = digit
    actual open fun setDigit(digit: Char) { this.digit = digit }

    actual open fun getPatternSeparator(): Char = patternSeparator
    actual open fun setPatternSeparator(patternSeparator: Char) { this.patternSeparator = patternSeparator }

    actual open fun getInfinity(): String = infinity
    actual open fun setInfinity(infinity: String) { this.infinity = infinity }

    actual open fun getNaN(): String = nan
    actual open fun setNaN(NaN: String) { this.nan = NaN }

    actual open fun getMinusSign(): Char = minusSign
    actual open fun setMinusSign(minusSign: Char) { this.minusSign = minusSign }

    actual open fun getCurrencySymbol(): String = currencySymbol
    actual open fun setCurrencySymbol(currency: String) { this.currencySymbol = currency }

    actual open fun getInternationalCurrencySymbol(): String = intlCurrencySymbol
    actual open fun setInternationalCurrencySymbol(currencyCode: String) { this.intlCurrencySymbol = currencyCode }

    actual open fun getMonetaryDecimalSeparator(): Char = monetarySeparator
    actual open fun setMonetaryDecimalSeparator(sep: Char) { this.monetarySeparator = sep }

    actual open fun getExponentSeparator(): String = exponentSeparator
    actual open fun setExponentSeparator(exp: String) {
        this.exponentSeparator = exp
    }

    actual open fun clone(): Any {
        val copy = DecimalFormatSymbols()
        copy.zeroDigit = zeroDigit
        copy.groupingSeparator = groupingSeparator
        copy.decimalSeparator = decimalSeparator
        copy.perMill = perMill
        copy.percent = percent
        copy.digit = digit
        copy.patternSeparator = patternSeparator
        copy.infinity = infinity
        copy.nan = nan
        copy.minusSign = minusSign
        copy.currencySymbol = currencySymbol
        copy.intlCurrencySymbol = intlCurrencySymbol
        copy.monetarySeparator = monetarySeparator
        copy.exponentSeparator = exponentSeparator
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecimalFormatSymbols) return false
        return zeroDigit == other.zeroDigit &&
            groupingSeparator == other.groupingSeparator &&
            decimalSeparator == other.decimalSeparator &&
            percent == other.percent &&
            perMill == other.perMill &&
            digit == other.digit &&
            minusSign == other.minusSign &&
            patternSeparator == other.patternSeparator &&
            infinity == other.infinity &&
            nan == other.nan &&
            currencySymbol == other.currencySymbol &&
            intlCurrencySymbol == other.intlCurrencySymbol &&
            monetarySeparator == other.monetarySeparator &&
            exponentSeparator == other.exponentSeparator
    }

    override fun hashCode(): Int {
        var result = zeroDigit.hashCode()
        result = 31 * result + groupingSeparator.hashCode()
        result = 31 * result + decimalSeparator.hashCode()
        return result
    }

    actual companion object {
        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
        actual fun getInstance(): DecimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault())
        actual fun getInstance(locale: Locale): DecimalFormatSymbols = DecimalFormatSymbols(locale)
    }
}
