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

    actual constructor(locale: Locale) {
        when (locale.language.lowercase()) {
            "zh" -> {
                eras = arrayOf("公元前", "公元")
                months = arrayOf(
                    "一月", "二月", "三月", "四月", "五月", "六月",
                    "七月", "八月", "九月", "十月", "十一月", "十二月", ""
                )
                shortMonths = arrayOf(
                    "一月", "二月", "三月", "四月", "五月", "六月",
                    "七月", "八月", "九月", "十月", "十一月", "十二月", ""
                )
                weekdays = arrayOf(
                    "", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
                )
                shortWeekdays = arrayOf(
                    "", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
                )
                ampms = arrayOf("上午", "下午")
            }
            "de" -> {
                eras = arrayOf("v. Chr.", "n. Chr.")
                months = arrayOf(
                    "Januar", "Februar", "März", "April", "Mai", "Juni",
                    "Juli", "August", "September", "Oktober", "November", "Dezember", ""
                )
                shortMonths = arrayOf(
                    "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
                    "Jul", "Aug", "Sep", "Okt", "Nov", "Dez", ""
                )
                weekdays = arrayOf(
                    "", "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"
                )
                shortWeekdays = arrayOf(
                    "", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"
                )
                ampms = arrayOf("vorm.", "nachm.")
            }
            "fr" -> {
                eras = arrayOf("av. J.-C.", "ap. J.-C.")
                months = arrayOf(
                    "janvier", "février", "mars", "avril", "mai", "juin",
                    "juillet", "août", "septembre", "octobre", "novembre", "décembre", ""
                )
                shortMonths = arrayOf(
                    "janv.", "févr.", "mars", "avr.", "mai", "juin",
                    "juil.", "août", "sept.", "oct.", "nov.", "déc.", ""
                )
                weekdays = arrayOf(
                    "", "dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"
                )
                shortWeekdays = arrayOf(
                    "", "dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."
                )
                ampms = arrayOf("AM", "PM")
            }
            "ja" -> {
                eras = arrayOf("紀元前", "西暦")
                months = arrayOf(
                    "1月", "2月", "3月", "4月", "5月", "6月",
                    "7月", "8月", "9月", "10月", "11月", "12月", ""
                )
                shortMonths = arrayOf(
                    "1月", "2月", "3月", "4月", "5月", "6月",
                    "7月", "8月", "9月", "10月", "11月", "12月", ""
                )
                weekdays = arrayOf(
                    "", "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日"
                )
                shortWeekdays = arrayOf(
                    "", "日", "月", "火", "水", "木", "金", "土"
                )
                ampms = arrayOf("午前", "午後")
            }
            "it" -> {
                eras = arrayOf("BC", "dopo Cristo")
                months = arrayOf(
                    "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
                    "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre", ""
                )
                shortMonths = arrayOf(
                    "gen", "feb", "mar", "apr", "mag", "giu",
                    "lug", "ago", "set", "ott", "nov", "dic", ""
                )
                weekdays = arrayOf(
                    "", "domenica", "lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato"
                )
                shortWeekdays = arrayOf(
                    "", "dom", "lun", "mar", "mer", "gio", "ven", "sab"
                )
                ampms = arrayOf("m.", "p.")
            }
            "ko" -> {
                eras = arrayOf("기원전", "서기")
                months = arrayOf(
                    "1월", "2월", "3월", "4월", "5월", "6월",
                    "7월", "8월", "9월", "10월", "11월", "12월", ""
                )
                shortMonths = arrayOf(
                    "1월", "2월", "3월", "4월", "5월", "6월",
                    "7월", "8월", "9월", "10월", "11월", "12월", ""
                )
                weekdays = arrayOf(
                    "", "일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"
                )
                shortWeekdays = arrayOf(
                    "", "일", "월", "화", "수", "목", "금", "토"
                )
                ampms = arrayOf("오전", "오후")
            }
        }
    }

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
