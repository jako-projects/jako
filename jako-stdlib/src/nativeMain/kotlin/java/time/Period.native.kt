package java.time

import java.io.Serializable
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAmount

actual class Period private constructor(
    actual val years: Int,
    actual val months: Int,
    actual val days: Int
) : TemporalAmount, Serializable {

    actual fun getYears(): Int = years
    actual fun getMonths(): Int = months
    actual fun getDays(): Int = days

    actual fun isZero(): Boolean = this === ZERO || (years == 0 && months == 0 && days == 0)

    actual fun isNegative(): Boolean = years < 0 || months < 0 || days < 0

    actual fun plus(amountToAdd: TemporalAmount): Period {
        // [Nullability Decision] amountToAdd is non-null; only Period is supported, matching Period.from
        val iso = fromAmount(amountToAdd)
        return of(
            addExact(years, iso.years),
            addExact(months, iso.months),
            addExact(days, iso.days)
        )
    }

    actual fun minus(amountToSubtract: TemporalAmount): Period {
        val iso = fromAmount(amountToSubtract)
        return of(
            addExact(years, -iso.years),
            addExact(months, -iso.months),
            addExact(days, -iso.days)
        )
    }

    actual fun plusYears(yearsToAdd: Long): Period {
        if (yearsToAdd == 0L) return this
        return of(toIntExact(addExact(years.toLong(), yearsToAdd)), months, days)
    }

    actual fun plusMonths(monthsToAdd: Long): Period {
        if (monthsToAdd == 0L) return this
        return of(years, toIntExact(addExact(months.toLong(), monthsToAdd)), days)
    }

    actual fun plusDays(daysToAdd: Long): Period {
        if (daysToAdd == 0L) return this
        return of(years, months, toIntExact(addExact(days.toLong(), daysToAdd)))
    }

    actual fun minusYears(yearsToSubtract: Long): Period {
        return if (yearsToSubtract == Long.MIN_VALUE) {
            plusYears(Long.MAX_VALUE).plusYears(1)
        } else {
            plusYears(-yearsToSubtract)
        }
    }

    actual fun minusMonths(monthsToSubtract: Long): Period {
        return if (monthsToSubtract == Long.MIN_VALUE) {
            plusMonths(Long.MAX_VALUE).plusMonths(1)
        } else {
            plusMonths(-monthsToSubtract)
        }
    }

    actual fun minusDays(daysToSubtract: Long): Period {
        return if (daysToSubtract == Long.MIN_VALUE) {
            plusDays(Long.MAX_VALUE).plusDays(1)
        } else {
            plusDays(-daysToSubtract)
        }
    }

    actual fun multipliedBy(scalar: Int): Period {
        if (this === ZERO || scalar == 1) return this
        return of(
            multiplyExact(years, scalar),
            multiplyExact(months, scalar),
            multiplyExact(days, scalar)
        )
    }

    actual fun negated(): Period = multipliedBy(-1)

    actual fun normalized(): Period {
        val totalMonths = toTotalMonths()
        val splitYears = totalMonths / 12L
        val splitMonths = (totalMonths % 12L).toInt()
        if (splitYears == years.toLong() && splitMonths == months) {
            return this
        }
        return of(toIntExact(splitYears), splitMonths, days)
    }

    actual fun toTotalMonths(): Long = years * 12L + months

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Period) return false
        return years == other.years && months == other.months && days == other.days
    }

    override fun hashCode(): Int {
        return years + rotateLeft(months, 8) + rotateLeft(days, 16)
    }

    override fun toString(): String {
        if (this === ZERO || (years == 0 && months == 0 && days == 0)) {
            return "P0D"
        }
        val buf = StringBuilder("P")
        if (years != 0) buf.append(years).append('Y')
        if (months != 0) buf.append(months).append('M')
        if (days != 0) buf.append(days).append('D')
        return buf.toString()
    }

    actual companion object {
        actual val ZERO: Period = Period(0, 0, 0)

        actual fun of(years: Int, months: Int, days: Int): Period {
            if ((years or months or days) == 0) {
                return ZERO
            }
            return Period(years, months, days)
        }

        actual fun ofYears(years: Int): Period = of(years, 0, 0)
        actual fun ofMonths(months: Int): Period = of(0, months, 0)
        actual fun ofWeeks(weeks: Int): Period = of(0, 0, multiplyExact(weeks, 7))
        actual fun ofDays(days: Int): Period = of(0, 0, days)

        actual fun between(startDateInclusive: LocalDate, endDateExclusive: LocalDate): Period {
            return startDateInclusive.until(endDateExclusive)
        }

        actual fun parse(text: CharSequence): Period {
            val s = text.toString()
            if (s.length < 3) throw DateTimeParseException("Text cannot be parsed to a Period", s, 0)
            var idx = 0
            var negative = false
            if (s[idx] == '+' || s[idx] == '-') {
                negative = s[idx] == '-'
                idx++
            }
            if (idx >= s.length || (s[idx] != 'P' && s[idx] != 'p')) {
                throw DateTimeParseException("Text cannot be parsed to a Period", s, idx)
            }
            idx++
            var years = 0
            var months = 0
            var weeks = 0
            var days = 0
            var parsedAny = false
            while (idx < s.length) {
                var sign = 1
                if (s[idx] == '+' || s[idx] == '-') {
                    if (s[idx] == '-') sign = -1
                    idx++
                }
                val numStart = idx
                while (idx < s.length && s[idx] in '0'..'9') idx++
                if (idx == numStart || idx >= s.length) {
                    throw DateTimeParseException("Text cannot be parsed to a Period", s, idx)
                }
                val num = s.substring(numStart, idx).toIntOrNull()
                    ?: throw DateTimeParseException("Text cannot be parsed to a Period", s, numStart)
                val unitChar = s[idx].uppercaseChar()
                idx++
                parsedAny = true
                when (unitChar) {
                    'Y' -> years = num * sign
                    'M' -> months = num * sign
                    'W' -> weeks = num * sign
                    'D' -> days = num * sign
                    else -> throw DateTimeParseException("Text cannot be parsed to a Period", s, idx - 1)
                }
            }
            if (!parsedAny) throw DateTimeParseException("Text cannot be parsed to a Period", s, 0)
            val totalDays = addExact(days, multiplyExact(weeks, 7))
            val result = of(years, months, totalDays)
            return if (negative) result.negated() else result
        }

        private fun fromAmount(amount: TemporalAmount): Period {
            if (amount is Period) return amount
            throw DateTimeException("Unable to obtain Period from TemporalAmount: $amount")
        }
    }
}

private fun rotateLeft(value: Int, distance: Int): Int {
    return (value shl distance) or (value ushr (32 - distance))
}
