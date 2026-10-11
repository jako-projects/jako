package java.time

import java.io.Serializable
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

        private fun fromAmount(amount: TemporalAmount): Period {
            if (amount is Period) return amount
            throw DateTimeException("Unable to obtain Period from TemporalAmount: $amount")
        }
    }
}

private fun rotateLeft(value: Int, distance: Int): Int {
    return (value shl distance) or (value ushr (32 - distance))
}
