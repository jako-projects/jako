@file:Suppress("OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable

actual class BigDecimal : Number, Comparable<BigDecimal>, Serializable {
    private val intVal: BigInteger
    private val scaleVal: Int

    actual constructor(unscaledVal: BigInteger, scale: Int) {
        this.intVal = unscaledVal
        this.scaleVal = scale
    }

    actual constructor(valBigInt: BigInteger) : this(valBigInt, 0)

    actual constructor(valStr: String) {
        val s = valStr.trim()
        val expIdx = s.indexOfAny(charArrayOf('e', 'E'))
        var exp = 0
        val numPart = if (expIdx != -1) {
            exp = s.substring(expIdx + 1).toInt()
            s.substring(0, expIdx)
        } else {
            s
        }

        val dotIdx = numPart.indexOf('.')
        if (dotIdx == -1) {
            this.intVal = BigInteger(numPart)
            this.scaleVal = -exp
        } else {
            val integerPart = numPart.substring(0, dotIdx)
            val fracPart = numPart.substring(dotIdx + 1)
            val combined = integerPart + fracPart
            this.intVal = BigInteger(combined)
            this.scaleVal = fracPart.length - exp
        }
    }

    actual constructor(valDouble: Double) : this(valDouble.toString())

    actual constructor(valLong: Long) : this(BigInteger.valueOf(valLong), 0)

    actual constructor(valInt: Int) : this(BigInteger.valueOf(valInt.toLong()), 0)

    actual fun unscaledValue(): BigInteger = intVal

    actual fun scale(): Int = scaleVal

    actual fun precision(): Int {
        val s = intVal.abs().toString()
        return s.length
    }

    actual fun signum(): Int = intVal.signum()

    actual fun abs(): BigDecimal = if (signum() < 0) negate() else this

    actual fun negate(): BigDecimal = BigDecimal(intVal.negate(), scaleVal)

    actual fun add(augend: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scaleVal, augend.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - augend.scaleVal

        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) augend.intVal.multiply(BigInteger.TEN.pow(diff2)) else augend.intVal
        return BigDecimal(u1.add(u2), maxScale)
    }

    actual fun subtract(subtrahend: BigDecimal): BigDecimal = add(subtrahend.negate())

    actual fun multiply(multiplicand: BigDecimal): BigDecimal {
        return BigDecimal(this.intVal.multiply(multiplicand.intVal), this.scaleVal + multiplicand.scaleVal)
    }

    actual fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        require(divisor.signum() != 0) { "Division by zero" }
        val shift = scale + divisor.scaleVal - this.scaleVal
        val shiftedNumerator = if (shift >= 0) {
            this.intVal.multiply(BigInteger.TEN.pow(shift))
        } else {
            this.intVal.divide(BigInteger.TEN.pow(-shift))
        }

        val (quotient, remainder) = shiftedNumerator.divideAndRemainder(divisor.intVal)
        if (remainder.signum() == 0) {
            return BigDecimal(quotient, scale)
        }

        val sign = this.signum() * divisor.signum()
        val roundUp = shouldRoundUp(quotient, remainder, divisor.intVal, roundingMode, sign)
        val finalQuotient = if (roundUp) {
            if (sign > 0) quotient.add(BigInteger.ONE) else quotient.subtract(BigInteger.ONE)
        } else {
            quotient
        }
        return BigDecimal(finalQuotient, scale)
    }

    private fun shouldRoundUp(
        quotient: BigInteger,
        remainder: BigInteger,
        divisor: BigInteger,
        mode: RoundingMode,
        sign: Int
    ): Boolean {
        when (mode) {
            RoundingMode.UP -> return true
            RoundingMode.DOWN -> return false
            RoundingMode.CEILING -> return sign > 0
            RoundingMode.FLOOR -> return sign < 0
            RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> {
                val cmp = remainder.abs().multiply(BigInteger.valueOf(2L)).compareTo(divisor.abs())
                if (cmp > 0) return true
                if (cmp < 0) return false
                if (mode == RoundingMode.HALF_UP) return true
                if (mode == RoundingMode.HALF_DOWN) return false
                // HALF_EVEN
                return quotient.testBit(0)
            }
        }
    }

    actual fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal {
        return divide(divisor, this.scaleVal, roundingMode)
    }

    actual fun divide(divisor: BigDecimal): BigDecimal {
        return divide(divisor, maxOf(this.scaleVal, divisor.scaleVal), RoundingMode.UNNECESSARY)
    }

    actual fun remainder(divisor: BigDecimal): BigDecimal {
        val q = divideToIntegralValue(divisor)
        return subtract(q.multiply(divisor))
    }

    private fun divideToIntegralValue(divisor: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scaleVal, divisor.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - divisor.scaleVal
        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) divisor.intVal.multiply(BigInteger.TEN.pow(diff2)) else divisor.intVal
        val q = u1.divide(u2)
        return BigDecimal(q, 0)
    }

    actual fun pow(n: Int): BigDecimal {
        require(n in 0..999999999) { "Invalid pow exponent: $n" }
        if (n == 0) return ONE
        return BigDecimal(this.intVal.pow(n), this.scaleVal * n)
    }

    actual fun setScale(newScale: Int): BigDecimal = setScale(newScale, RoundingMode.UNNECESSARY)

    actual fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (newScale == this.scaleVal) return this
        if (newScale > this.scaleVal) {
            val diff = newScale - this.scaleVal
            return BigDecimal(this.intVal.multiply(BigInteger.TEN.pow(diff)), newScale)
        } else {
            val diff = this.scaleVal - newScale
            val div = BigInteger.TEN.pow(diff)
            val (q, r) = this.intVal.divideAndRemainder(div)
            if (r.signum() == 0) return BigDecimal(q, newScale)
            val roundUp = shouldRoundUp(q, r, div, roundingMode, this.signum())
            val finalQ = if (roundUp) (if (signum() > 0) q.add(BigInteger.ONE) else q.subtract(BigInteger.ONE)) else q
            return BigDecimal(finalQ, newScale)
        }
    }

    actual fun stripTrailingZeros(): BigDecimal {
        if (intVal.signum() == 0) return BigDecimal.ZERO
        var curInt = intVal
        var curScale = scaleVal
        while (curScale > 0) {
            val (q, r) = curInt.divideAndRemainder(BigInteger.TEN)
            if (r.signum() != 0) break
            curInt = q
            curScale--
        }
        return BigDecimal(curInt, curScale)
    }

    actual fun movePointLeft(n: Int): BigDecimal = BigDecimal(intVal, scaleVal + n)

    actual fun movePointRight(n: Int): BigDecimal = BigDecimal(intVal, scaleVal - n)

    actual fun toPlainString(): String {
        if (scaleVal == 0) return intVal.toString()
        val isNeg = intVal.signum() < 0
        var s = intVal.abs().toString()
        val sb = StringBuilder()
        if (isNeg) sb.append('-')

        if (scaleVal > 0) {
            if (s.length <= scaleVal) {
                sb.append("0.")
                val zeros = scaleVal - s.length
                for (i in 0 until zeros) sb.append('0')
                sb.append(s)
            } else {
                val dotPos = s.length - scaleVal
                sb.append(s.substring(0, dotPos))
                sb.append('.')
                sb.append(s.substring(dotPos))
            }
        } else {
            sb.append(s)
            for (i in 0 until -scaleVal) sb.append('0')
        }
        return sb.toString()
    }

    actual fun toEngineeringString(): String = toPlainString()

    actual override fun toString(): String = toPlainString()

    actual override fun toByte(): Byte = intVal.toByte()
    actual override fun toShort(): Short = intVal.toShort()
    actual override fun toInt(): Int = intVal.toInt()
    actual override fun toLong(): Long = intVal.toLong()
    actual override fun toFloat(): Float = toDouble().toFloat()
    actual override fun toDouble(): Double = toString().toDouble()
    actual override fun toChar(): Char = intVal.toChar()

    actual override fun compareTo(other: BigDecimal): Int {
        if (this.scaleVal == other.scaleVal) {
            return this.intVal.compareTo(other.intVal)
        }
        val maxScale = maxOf(this.scaleVal, other.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - other.scaleVal
        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) other.intVal.multiply(BigInteger.TEN.pow(diff2)) else other.intVal
        return u1.compareTo(u2)
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigDecimal) return false
        return this.scaleVal == other.scaleVal && this.intVal == other.intVal
    }

    actual override fun hashCode(): Int = 31 * intVal.hashCode() + scaleVal

    actual companion object {
        actual val ZERO: BigDecimal = BigDecimal(BigInteger.ZERO, 0)
        actual val ONE: BigDecimal = BigDecimal(BigInteger.ONE, 0)
        actual val TEN: BigDecimal = BigDecimal(BigInteger.TEN, 0)

        actual fun valueOf(valLong: Long): BigDecimal = BigDecimal(BigInteger.valueOf(valLong), 0)

        actual fun valueOf(unscaledVal: Long, scale: Int): BigDecimal =
            BigDecimal(BigInteger.valueOf(unscaledVal), scale)

        actual fun valueOf(valDouble: Double): BigDecimal = BigDecimal(valDouble.toString())
    }
}
