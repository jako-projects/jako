package java.time

internal const val NANOS_PER_SECOND = 1_000_000_000L
internal const val NANOS_PER_MINUTE = NANOS_PER_SECOND * 60L
internal const val NANOS_PER_HOUR = NANOS_PER_MINUTE * 60L
internal const val NANOS_PER_DAY = NANOS_PER_HOUR * 24L
internal const val SECONDS_PER_DAY = 86400L
internal const val MINUTES_PER_DAY = 1440L
internal const val HOURS_PER_DAY = 24L
internal const val DAYS_PER_CYCLE = 146097
internal const val DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L)
internal const val YEAR_MIN = -999_999_999
internal const val YEAR_MAX = 999_999_999

internal fun isLeapYear(year: Int): Boolean {
    return (year and 3) == 0 && ((year % 100) != 0 || (year % 400) == 0)
}

internal fun lengthOfMonth(year: Int, month: Int): Int {
    return when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

internal fun lengthOfYear(year: Int): Int = if (isLeapYear(year)) 366 else 365

internal fun addExact(a: Long, b: Long): Long {
    val r = a + b
    if ((a xor r) and (b xor r) < 0) {
        throw ArithmeticException("long overflow")
    }
    return r
}

internal fun addExact(a: Int, b: Int): Int {
    val r = a.toLong() + b.toLong()
    if (r != r.toInt().toLong()) {
        throw ArithmeticException("integer overflow")
    }
    return r.toInt()
}

internal fun multiplyExact(a: Int, b: Int): Int {
    val r = a.toLong() * b.toLong()
    if (r != r.toInt().toLong()) {
        throw ArithmeticException("integer overflow")
    }
    return r.toInt()
}

internal fun toIntExact(value: Long): Int {
    val i = value.toInt()
    if (i.toLong() != value) {
        throw ArithmeticException("integer overflow")
    }
    return i
}

internal fun checkYear(year: Long): Int {
    if (year < YEAR_MIN || year > YEAR_MAX) {
        throw DateTimeException("Invalid year: $year")
    }
    return year.toInt()
}

internal fun formatIsoDate(year: Int, month: Int, day: Int): String {
    val absYear = if (year < 0) -year else year
    val sb = StringBuilder(10)
    if (absYear < 1000) {
        if (year < 0) {
            sb.append(year - 10000).deleteAt(1)
        } else {
            sb.append(year + 10000).deleteAt(0)
        }
    } else {
        if (year > 9999) {
            sb.append('+')
        }
        sb.append(year)
    }
    sb.append(if (month < 10) "-0" else "-").append(month)
    sb.append(if (day < 10) "-0" else "-").append(day)
    return sb.toString()
}

internal fun formatIsoTime(hour: Int, minute: Int, second: Int, nano: Int, alwaysSeconds: Boolean): String {
    val sb = StringBuilder(18)
    sb.append(if (hour < 10) "0" else "").append(hour)
    sb.append(if (minute < 10) ":0" else ":").append(minute)
    if (alwaysSeconds || second > 0 || nano > 0) {
        sb.append(if (second < 10) ":0" else ":").append(second)
        if (nano > 0) {
            sb.append('.')
            sb.append(formatNanoDigits(nano))
        }
    }
    return sb.toString()
}

internal fun formatNanoDigits(nano: Int): String {
    return when {
        nano % 1_000_000 == 0 -> ((nano / 1_000_000) + 1000).toString().substring(1)
        nano % 1_000 == 0 -> ((nano / 1_000) + 1_000_000).toString().substring(1)
        else -> (nano + 1_000_000_000).toString().substring(1)
    }
}

internal fun formatIsoInstantFraction(nano: Int): String {
    if (nano == 0) return ""
    val padded = nano.toString().padStart(9, '0')
    return when {
        nano % 1_000_000 == 0 -> "." + padded.substring(0, 3)
        nano % 1_000 == 0 -> "." + padded.substring(0, 6)
        else -> ".$padded"
    }
}
