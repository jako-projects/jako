package java.time

import java.time.temporal.TemporalAccessor

actual enum class Month : TemporalAccessor {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    actual val value: Int get() = ordinal + 1
    actual fun getValue(): Int = value

    actual companion object {
        actual fun of(month: Int): Month {
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for MonthOfYear: $month")
            }
            return values()[month - 1]
        }
    }
}
