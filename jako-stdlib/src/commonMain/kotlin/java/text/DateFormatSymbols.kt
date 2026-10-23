@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

import java.io.Serializable
import java.lang.Cloneable
import java.util.Locale

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
