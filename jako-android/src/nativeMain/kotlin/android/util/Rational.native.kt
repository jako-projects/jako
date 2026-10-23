package android.util

import kotlin.math.abs

actual final class Rational actual constructor(
    numerator: Int,
    denominator: Int
) : Number(), Comparable<Rational> {

    actual val numerator: Int
    actual val denominator: Int

    init {
        var num = numerator
        var den = denominator
        if (den < 0) {
            num = -num
            den = -den
        }
        if (den == 0 && num > 0) {
            this.numerator = 1
            this.denominator = 0
        } else if (den == 0 && num < 0) {
            this.numerator = -1
            this.denominator = 0
        } else if (den == 0 && num == 0) {
            this.numerator = 0
            this.denominator = 0
        } else if (num == 0) {
            this.numerator = 0
            this.denominator = 1
        } else {
            val gcd = gcd(num, den)
            this.numerator = num / gcd
            this.denominator = den / gcd
        }
    }

    actual fun getNumerator(): Int = numerator
    actual fun getDenominator(): Int = denominator

    actual val isNaN: Boolean
        get() = denominator == 0 && numerator == 0

    actual fun isNaN(): Boolean = isNaN

    actual val isInfinite: Boolean
        get() = numerator != 0 && denominator == 0

    actual fun isInfinite(): Boolean = isInfinite

    actual val isFinite: Boolean
        get() = denominator != 0

    actual fun isFinite(): Boolean = isFinite

    actual val isZero: Boolean
        get() = isFinite && numerator == 0

    actual fun isZero(): Boolean = isZero

    private val isPosInf: Boolean
        get() = denominator == 0 && numerator > 0

    private val isNegInf: Boolean
        get() = denominator == 0 && numerator < 0

    actual override fun equals(other: Any?): Boolean {
        return other is Rational && numerator == other.numerator && denominator == other.denominator
    }

    actual override fun toString(): String {
        return when {
            isNaN -> "NaN"
            isPosInf -> "Infinity"
            isNegInf -> "-Infinity"
            else -> "$numerator/$denominator"
        }
    }

    actual override fun hashCode(): Int {
        val numeratorFlipped = (numerator shl 16) or (numerator ushr 16)
        return denominator xor numeratorFlipped
    }

    actual override fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    actual override fun toFloat(): Float = numerator.toFloat() / denominator.toFloat()

    actual override fun toInt(): Int = when {
        isPosInf -> Int.MAX_VALUE
        isNegInf -> Int.MIN_VALUE
        isNaN -> 0
        else -> numerator / denominator
    }

    actual override fun toLong(): Long = when {
        isPosInf -> Long.MAX_VALUE
        isNegInf -> Long.MIN_VALUE
        isNaN -> 0L
        else -> (numerator / denominator).toLong()
    }

    actual override fun toShort(): Short = toInt().toShort()

    actual override fun toByte(): Byte = toInt().toByte()

    @Suppress("DEPRECATION")
    actual override fun toChar(): Char = toInt().toChar()

    actual override fun compareTo(other: Rational): Int {
        if (equals(other)) return 0
        if (isNaN) return 1
        if (other.isNaN) return -1
        if (isPosInf || other.isNegInf) return 1
        if (isNegInf || other.isPosInf) return -1

        val thisNumerator = numerator.toLong() * other.denominator.toLong()
        val otherNumerator = other.numerator.toLong() * denominator.toLong()
        return thisNumerator.compareTo(otherNumerator)
    }

    actual companion object {
        actual val NaN: Rational = Rational(0, 0)
        actual val POSITIVE_INFINITY: Rational = Rational(1, 0)
        actual val NEGATIVE_INFINITY: Rational = Rational(-1, 0)
        actual val ZERO: Rational = Rational(0, 1)

        fun gcd(numerator: Int, denominator: Int): Int {
            var a = numerator
            var b = denominator
            while (b != 0) {
                val oldB = b
                b = a % b
                a = oldB
            }
            return abs(a)
        }

        private fun invalidRational(s: String): Nothing {
            throw NumberFormatException("Invalid Rational: \"$s\"")
        }

        actual fun parseRational(string: String): Rational {
            when (string) {
                "NaN" -> return NaN
                "Infinity" -> return POSITIVE_INFINITY
                "-Infinity" -> return NEGATIVE_INFINITY
            }
            var sepIx = string.indexOf(':')
            if (sepIx < 0) {
                sepIx = string.indexOf('/')
            }
            if (sepIx < 0) {
                invalidRational(string)
            }
            try {
                return Rational(
                    string.substring(0, sepIx).toInt(),
                    string.substring(sepIx + 1).toInt()
                )
            } catch (e: NumberFormatException) {
                invalidRational(string)
            }
        }
    }
}
