package java.text

import java.io.Serializable
import java.lang.Cloneable
import java.util.Locale

actual open class DateFormatSymbols : Serializable, Cloneable {
    private var eras: Array<String> = arrayOf("BC", "AD")
    private var months: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December", ""
    )
    private var shortMonths: Array<String> = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""
    )
    private var weekdays: Array<String> = arrayOf(
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )
    private var shortWeekdays: Array<String> = arrayOf(
        "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    )
    private var ampms: Array<String> = arrayOf("AM", "PM")
    private var zoneStrings: Array<Array<String>> = arrayOf(
        arrayOf("UTC", "Coordinated Universal Time", "UTC", "Coordinated Universal Time", "UTC")
    )
    private var localPatternChars: String = "GyMdkHmsSEDFwWahKzZ"

    actual constructor() : this(Locale.getDefault())

    actual constructor(locale: Locale)

    actual open fun getEras(): Array<String> = eras.copyOf()
    actual open fun setEras(newEras: Array<String>) { eras = newEras.copyOf() }

    actual open fun getMonths(): Array<String> = months.copyOf()
    actual open fun setMonths(newMonths: Array<String>) { months = newMonths.copyOf() }

    actual open fun getShortMonths(): Array<String> = shortMonths.copyOf()
    actual open fun setShortMonths(newShortMonths: Array<String>) { shortMonths = newShortMonths.copyOf() }

    actual open fun getWeekdays(): Array<String> = weekdays.copyOf()
    actual open fun setWeekdays(newWeekdays: Array<String>) { weekdays = newWeekdays.copyOf() }

    actual open fun getShortWeekdays(): Array<String> = shortWeekdays.copyOf()
    actual open fun setShortWeekdays(newShortWeekdays: Array<String>) { shortWeekdays = newShortWeekdays.copyOf() }

    actual open fun getAmPmStrings(): Array<String> = ampms.copyOf()
    actual open fun setAmPmStrings(newAmpms: Array<String>) { ampms = newAmpms.copyOf() }

    actual open fun getZoneStrings(): Array<Array<String>> =
        Array(zoneStrings.size) { zoneStrings[it].copyOf() }

    actual open fun setZoneStrings(newZoneStrings: Array<Array<String>>) {
        for (row in newZoneStrings) {
            if (row.size < 5) throw IllegalArgumentException("Each zone string row must have at least 5 elements")
        }
        zoneStrings = Array(newZoneStrings.size) { newZoneStrings[it].copyOf() }
    }

    actual open fun getLocalPatternChars(): String = localPatternChars
    actual open fun setLocalPatternChars(newLocalPatternChars: String) {
        localPatternChars = newLocalPatternChars
    }

    actual open fun clone(): Any {
        val copy = DateFormatSymbols()
        copy.eras = eras.copyOf()
        copy.months = months.copyOf()
        copy.shortMonths = shortMonths.copyOf()
        copy.weekdays = weekdays.copyOf()
        copy.shortWeekdays = shortWeekdays.copyOf()
        copy.ampms = ampms.copyOf()
        copy.zoneStrings = Array(zoneStrings.size) { zoneStrings[it].copyOf() }
        copy.localPatternChars = localPatternChars
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateFormatSymbols) return false
        return eras.contentEquals(other.eras) &&
            months.contentEquals(other.months) &&
            shortMonths.contentEquals(other.shortMonths) &&
            weekdays.contentEquals(other.weekdays) &&
            shortWeekdays.contentEquals(other.shortWeekdays) &&
            ampms.contentEquals(other.ampms) &&
            localPatternChars == other.localPatternChars
    }

    override fun hashCode(): Int = months.contentHashCode()

    actual companion object {
        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
        actual fun getInstance(): DateFormatSymbols = DateFormatSymbols(Locale.getDefault())
        actual fun getInstance(locale: Locale): DateFormatSymbols = DateFormatSymbols(locale)
    }
}
