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

    actual constructor(unscaledVal: BigInteger, scale: Int, mc: MathContext) {
        val raw = BigDecimal(unscaledVal, scale).round(mc)
        this.intVal = raw.intVal
        this.scaleVal = raw.scaleVal
    }

    actual constructor(valBigInt: BigInteger) : this(valBigInt, 0)

    actual constructor(valBigInt: BigInteger, mc: MathContext) : this(valBigInt, 0, mc)

    actual constructor(valStr: String) {
        if (valStr.isEmpty()) throw NumberFormatException("Empty BigDecimal")
        val expIdx = valStr.indexOfAny(charArrayOf('e', 'E'))
        var exp = 0L
        val numPart = if (expIdx != -1) {
            val expStr = valStr.substring(expIdx + 1)
            if (expStr.isEmpty()) throw NumberFormatException("Invalid exponent in $valStr")
            exp = expStr.toLongOrNull() ?: throw NumberFormatException("Invalid exponent in $valStr")
            valStr.substring(0, expIdx)
        } else {
            valStr
        }

        if (numPart.isEmpty()) throw NumberFormatException("No digits in $valStr")
        val dotIdx = numPart.indexOf('.')
        if (dotIdx == -1) {
            this.intVal = BigInteger(numPart)
            this.scaleVal = checkScale(-exp)
        } else {
            if (numPart.indexOf('.', dotIdx + 1) != -1) {
                throw NumberFormatException("Multiple decimal points in $valStr")
            }
            val integerPart = numPart.substring(0, dotIdx)
            val fracPart = numPart.substring(dotIdx + 1)
            if (fracPart.any { it == '+' || it == '-' }) {
                throw NumberFormatException("Sign in fraction part in $valStr")
            }
            val combined = integerPart + fracPart
            if (combined.isEmpty() || combined == "+" || combined == "-") {
                throw NumberFormatException("No digits in $valStr")
            }
            this.intVal = BigInteger(combined)
            this.scaleVal = checkScale(fracPart.length.toLong() - exp)
        }
    }

    actual constructor(valStr: String, mc: MathContext) {
        val raw = BigDecimal(valStr).round(mc)
        this.intVal = raw.intVal
        this.scaleVal = raw.scaleVal
    }

    actual constructor(valDouble: Double) {
        if (valDouble.isInfinite() || valDouble.isNaN()) {
            throw NumberFormatException("Infinite or NaN")
        }
        val bits = valDouble.toRawBits()
        val sign = if ((bits ushr 63) == 0L) 1 else -1
        val exponent = ((bits ushr 52) and 0x7ffL).toInt()
        var significand = if (exponent == 0) {
            (bits and ((1L shl 52) - 1L)) shl 1
        } else {
            (bits and ((1L shl 52) - 1L)) or (1L shl 52)
        }
        var binExp = exponent - 1075
        if (significand == 0L) {
            this.intVal = BigInteger.ZERO
            this.scaleVal = 0
        } else {
            while ((significand and 1L) == 0L) {
                significand = significand ushr 1
                binExp++
            }
            val base = BigInteger.valueOf(if (sign < 0) -significand else significand)
            if (binExp >= 0) {
                this.intVal = base.multiply(BigInteger.valueOf(2L).pow(binExp))
                this.scaleVal = 0
            } else {
                this.intVal = base.multiply(BigInteger.valueOf(5L).pow(-binExp))
                this.scaleVal = -binExp
            }
        }
    }

    actual constructor(valDouble: Double, mc: MathContext) {
        val raw = BigDecimal(valDouble).round(mc)
        this.intVal = raw.intVal
        this.scaleVal = raw.scaleVal
    }

    actual constructor(valLong: Long) : this(BigInteger.valueOf(valLong), 0)

    actual constructor(valLong: Long, mc: MathContext) : this(BigInteger.valueOf(valLong), 0, mc)

    actual constructor(valInt: Int) : this(BigInteger.valueOf(valInt.toLong()), 0)

    actual constructor(valInt: Int, mc: MathContext) : this(BigInteger.valueOf(valInt.toLong()), 0, mc)

    private fun checkScale(s: Long): Int {
        if (s > Int.MAX_VALUE || s < Int.MIN_VALUE) {
            throw ArithmeticException(if (s > Int.MAX_VALUE) "Underflow" else "Overflow")
        }
        return s.toInt()
    }

    actual fun unscaledValue(): BigInteger = intVal

    actual fun scale(): Int = scaleVal

    actual fun precision(): Int {
        if (intVal.signum() == 0) return 1
        val s = intVal.abs().toString()
        return s.length
    }

    actual fun signum(): Int = intVal.signum()

    actual fun abs(): BigDecimal = if (signum() < 0) negate() else this

    actual fun abs(mc: MathContext): BigDecimal = abs().round(mc)

    actual fun negate(): BigDecimal = BigDecimal(intVal.negate(), scaleVal)

    actual fun negate(mc: MathContext): BigDecimal = negate().round(mc)

    actual fun plus(): BigDecimal = this

    actual fun plus(mc: MathContext): BigDecimal = round(mc)

    actual fun round(mc: MathContext): BigDecimal {
        val mcp = mc.precision
        if (mcp == 0) return this
        var result = this
        var prec = result.precision()
        while (prec > mcp) {
            val drop = prec - mcp
            result = result.setScale(checkScale(result.scaleVal.toLong() - drop.toLong()), mc.roundingMode)
            prec = result.precision()
        }
        return result
    }

    actual fun add(augend: BigDecimal): BigDecimal {
        val maxScale = maxOf(this.scaleVal, augend.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - augend.scaleVal

        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) augend.intVal.multiply(BigInteger.TEN.pow(diff2)) else augend.intVal
        return BigDecimal(u1.add(u2), maxScale)
    }

    actual fun add(augend: BigDecimal, mc: MathContext): BigDecimal = add(augend).round(mc)

    actual fun subtract(subtrahend: BigDecimal): BigDecimal = add(subtrahend.negate())

    actual fun subtract(subtrahend: BigDecimal, mc: MathContext): BigDecimal = subtract(subtrahend).round(mc)

    actual fun multiply(multiplicand: BigDecimal): BigDecimal {
        val newScale = checkScale(this.scaleVal.toLong() + multiplicand.scaleVal.toLong())
        return BigDecimal(this.intVal.multiply(multiplicand.intVal), newScale)
    }

    actual fun multiply(multiplicand: BigDecimal, mc: MathContext): BigDecimal = multiply(multiplicand).round(mc)

    actual fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal {
        if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        val shift = checkScale(scale.toLong() + divisor.scaleVal.toLong() - this.scaleVal.toLong())
        val shiftedNumerator: BigInteger
        val shiftedDenominator: BigInteger
        if (shift >= 0) {
            shiftedNumerator = this.intVal.multiply(BigInteger.TEN.pow(shift))
            shiftedDenominator = divisor.intVal
        } else {
            shiftedNumerator = this.intVal
            shiftedDenominator = divisor.intVal.multiply(BigInteger.TEN.pow(-shift))
        }

        val (quotient, remainder) = shiftedNumerator.divideAndRemainder(shiftedDenominator)
        if (remainder.signum() == 0) {
            return BigDecimal(quotient, scale)
        }

        val sign = this.signum() * divisor.signum()
        val roundUp = shouldRoundUp(quotient, remainder, shiftedDenominator, roundingMode, sign)
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
        if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        val preferredScale = checkScale(this.scaleVal.toLong() - divisor.scaleVal.toLong())
        if (this.signum() == 0) return BigDecimal(BigInteger.ZERO, preferredScale)

        val num = this.intVal.abs()
        val den = divisor.intVal.abs()
        val g = num.gcd(den)
        var denReduced = den.divide(g)
        val two = BigInteger.valueOf(2L)
        val five = BigInteger.valueOf(5L)
        var p2 = 0
        while (denReduced.mod(two).signum() == 0) {
            denReduced = denReduced.divide(two)
            p2++
        }
        var p5 = 0
        while (denReduced.mod(five).signum() == 0) {
            denReduced = denReduced.divide(five)
            p5++
        }
        if (denReduced != BigInteger.ONE) {
            throw ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.")
        }
        val extraScale = maxOf(p2, p5)
        val targetScale = checkScale(preferredScale.toLong() + extraScale.toLong())
        val scaledNum = if (extraScale > 0) this.intVal.multiply(BigInteger.TEN.pow(extraScale)) else this.intVal
        val quotient = scaledNum.divide(divisor.intVal)
        return BigDecimal(quotient, targetScale)
    }

    actual fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal {
        val mcp = mc.precision
        if (mcp == 0) return divide(divisor)
        if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        val preferredScale = checkScale(this.scaleVal.toLong() - divisor.scaleVal.toLong())
        if (this.signum() == 0) return BigDecimal(BigInteger.ZERO, preferredScale)

        val x = this.intVal.abs()
        val y = divisor.intVal.abs()
        val p1 = this.precision()
        val p2 = divisor.precision()
        val cmp = if (p1 >= p2) {
            x.compareTo(y.multiply(BigInteger.TEN.pow(p1 - p2)))
        } else {
            x.multiply(BigInteger.TEN.pow(p2 - p1)).compareTo(y)
        }
        val shift = if (cmp >= 0) mcp - p1 + p2 - 1 else mcp - p1 + p2
        val targetScale = checkScale(preferredScale.toLong() + shift.toLong())

        val num: BigInteger
        val den: BigInteger
        if (shift >= 0) {
            num = this.intVal.multiply(BigInteger.TEN.pow(shift))
            den = divisor.intVal
        } else {
            num = this.intVal
            den = divisor.intVal.multiply(BigInteger.TEN.pow(-shift))
        }
        val (quotient, remainder) = num.divideAndRemainder(den)
        if (remainder.signum() == 0) {
            var curInt = quotient
            var curScale = targetScale
            while (curScale > preferredScale && curInt.signum() != 0) {
                val (q, r) = curInt.divideAndRemainder(BigInteger.TEN)
                if (r.signum() != 0) break
                curInt = q
                curScale--
            }
            return BigDecimal(curInt, curScale).round(mc)
        }
        val sign = this.signum() * divisor.signum()
        val roundUp = shouldRoundUp(quotient, remainder, den, mc.roundingMode, sign)
        val finalQuotient = if (roundUp) {
            if (sign > 0) quotient.add(BigInteger.ONE) else quotient.subtract(BigInteger.ONE)
        } else {
            quotient
        }
        return BigDecimal(finalQuotient, targetScale).round(mc)
    }

    actual fun divideToIntegralValue(divisor: BigDecimal): BigDecimal {
        if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        val preferredScale = checkScale(this.scaleVal.toLong() - divisor.scaleVal.toLong())
        if (this.abs().compareTo(divisor.abs()) < 0) {
            return BigDecimal(BigInteger.ZERO, maxOf(0, preferredScale))
        }
        val maxScale = maxOf(this.scaleVal, divisor.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - divisor.scaleVal
        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) divisor.intVal.multiply(BigInteger.TEN.pow(diff2)) else divisor.intVal
        val q = u1.divide(u2)
        if (preferredScale > 0) {
            return BigDecimal(q.multiply(BigInteger.TEN.pow(preferredScale)), preferredScale)
        }
        if (preferredScale < 0) {
            var v = q
            var s = 0
            while (s > preferredScale && v.signum() != 0) {
                val qr = v.divideAndRemainder(BigInteger.TEN)
                if (qr[1].signum() == 0) {
                    v = qr[0]
                    s--
                } else {
                    break
                }
            }
            return BigDecimal(v, s)
        }
        return BigDecimal(q, 0)
    }

    actual fun divideToIntegralValue(divisor: BigDecimal, mc: MathContext): BigDecimal {
        if (mc.precision == 0 || this.abs().compareTo(divisor.abs()) < 0) {
            return divideToIntegralValue(divisor)
        }
        if (divisor.signum() == 0) throw ArithmeticException("Division by zero")
        val preferredScale = checkScale(this.scaleVal.toLong() - divisor.scaleVal.toLong())
        val maxScale = maxOf(this.scaleVal, divisor.scaleVal)
        val diff1 = maxScale - this.scaleVal
        val diff2 = maxScale - divisor.scaleVal
        val u1 = if (diff1 > 0) this.intVal.multiply(BigInteger.TEN.pow(diff1)) else this.intVal
        val u2 = if (diff2 > 0) divisor.intVal.multiply(BigInteger.TEN.pow(diff2)) else divisor.intVal
        val q = u1.divide(u2)
        val qBd = BigDecimal(q, 0)
        if (qBd.precision() > mc.precision) {
            throw ArithmeticException("Division impossible")
        }
        if (preferredScale > 0) {
            val padZeros = minOf(preferredScale, mc.precision - qBd.precision())
            return if (padZeros > 0) {
                BigDecimal(q.multiply(BigInteger.TEN.pow(padZeros)), padZeros)
            } else {
                qBd
            }
        }
        if (preferredScale < 0) {
            var v = q
            var s = 0
            while (s > preferredScale && v.signum() != 0) {
                val qr = v.divideAndRemainder(BigInteger.TEN)
                if (qr[1].signum() == 0) {
                    v = qr[0]
                    s--
                } else {
                    break
                }
            }
            return BigDecimal(v, s)
        }
        return qBd
    }

    actual fun remainder(divisor: BigDecimal): BigDecimal {
        val q = divideToIntegralValue(divisor)
        return subtract(q.multiply(divisor))
    }

    actual fun remainder(divisor: BigDecimal, mc: MathContext): BigDecimal {
        return divideAndRemainder(divisor, mc)[1]
    }

    actual fun divideAndRemainder(divisor: BigDecimal): Array<BigDecimal> {
        val q = divideToIntegralValue(divisor)
        val r = subtract(q.multiply(divisor))
        return arrayOf(q, r)
    }

    actual fun divideAndRemainder(divisor: BigDecimal, mc: MathContext): Array<BigDecimal> {
        if (mc.precision == 0) return divideAndRemainder(divisor)
        val q = divideToIntegralValue(divisor, mc)
        val r = subtract(q.multiply(divisor))
        return arrayOf(q, r)
    }

    actual fun pow(n: Int): BigDecimal {
        if (n < 0 || n > 999999999) {
            throw ArithmeticException("Invalid Operation")
        }
        if (n == 0) return ONE
        return BigDecimal(this.intVal.pow(n), checkScale(this.scaleVal.toLong() * n.toLong()))
    }

    actual fun pow(n: Int, mc: MathContext): BigDecimal {
        if (mc.precision == 0) return pow(n)
        if (n < -999999999 || n > 999999999) {
            throw ArithmeticException("Invalid Operation")
        }
        if (n == 0) return ONE
        var lhs = this
        var workMc = mc
        var mag = kotlin.math.abs(n)
        if (mc.precision > 0) {
            val elength = mag.toString().length
            if (elength > mc.precision) {
                throw ArithmeticException("Invalid Operation")
            }
            workMc = MathContext(mc.precision + elength + 1, mc.roundingMode)
        }
        var acc = ONE
        var seenBit = false
        var i = 1
        while (true) {
            mag += mag
            if (mag < 0) {
                seenBit = true
                acc = acc.multiply(lhs, workMc)
            }
            if (i == 31) break
            if (seenBit) {
                acc = acc.multiply(acc, workMc)
            }
            i++
        }
        if (n < 0) {
            acc = ONE.divide(acc, workMc)
        }
        return acc.round(mc)
    }

    actual fun setScale(newScale: Int): BigDecimal = setScale(newScale, RoundingMode.UNNECESSARY)

    actual fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
        if (newScale == this.scaleVal) return this
        if (newScale > this.scaleVal) {
            val diff = checkScale(newScale.toLong() - this.scaleVal.toLong())
            return BigDecimal(this.intVal.multiply(BigInteger.TEN.pow(diff)), newScale)
        } else {
            val diff = checkScale(this.scaleVal.toLong() - newScale.toLong())
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
        while (curInt.signum() != 0) {
            val (q, r) = curInt.divideAndRemainder(BigInteger.TEN)
            if (r.signum() != 0) break
            curInt = q
            curScale = checkScale(curScale.toLong() - 1L)
        }
        return BigDecimal(curInt, curScale)
    }

    actual fun movePointLeft(n: Int): BigDecimal = BigDecimal(intVal, checkScale(scaleVal.toLong() + n.toLong()))

    actual fun movePointRight(n: Int): BigDecimal = BigDecimal(intVal, checkScale(scaleVal.toLong() - n.toLong()))

    actual fun scaleByPowerOfTen(n: Int): BigDecimal = BigDecimal(intVal, checkScale(scaleVal.toLong() - n.toLong()))

    actual fun ulp(): BigDecimal = BigDecimal(BigInteger.ONE, scaleVal)

    actual fun min(valOther: BigDecimal): BigDecimal = if (compareTo(valOther) <= 0) this else valOther

    actual fun max(valOther: BigDecimal): BigDecimal = if (compareTo(valOther) >= 0) this else valOther

    actual fun toPlainString(): String {
        if (scaleVal == 0) return intVal.toString()
        val isNeg = intVal.signum() < 0
        if (intVal.signum() == 0) {
            if (scaleVal < 0) return "0"
            val sb = StringBuilder("0.")
            for (i in 0 until scaleVal) sb.append('0')
            return sb.toString()
        }
        val s = intVal.abs().toString()
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

    private fun layoutChars(sci: Boolean): String {
        if (scaleVal == 0) return intVal.toString()
        val coeff = intVal.abs().toString()
        val adjusted = -scaleVal.toLong() + (coeff.length - 1).toLong()
        if (scaleVal >= 0 && adjusted >= -6L) {
            return toPlainString()
        }
        val sb = StringBuilder()
        if (intVal.signum() < 0) sb.append('-')
        if (sci) {
            sb.append(coeff[0])
            if (coeff.length > 1) {
                sb.append('.')
                sb.append(coeff.substring(1))
            }
            if (adjusted != 0L) {
                sb.append('E')
                if (adjusted > 0) sb.append('+')
                sb.append(adjusted)
            }
        } else {
            var sig = (adjusted % 3).toInt()
            if (sig < 0) sig += 3
            var adj = adjusted - sig
            sig++
            if (intVal.signum() == 0) {
                when (sig) {
                    1 -> sb.append("0")
                    2 -> {
                        sb.append("0.00")
                        adj += 3
                    }
                    3 -> {
                        sb.append("0.0")
                        adj += 3
                    }
                }
            } else if (sig >= coeff.length) {
                sb.append(coeff)
                for (i in 0 until (sig - coeff.length)) sb.append('0')
            } else {
                sb.append(coeff.substring(0, sig))
                sb.append('.')
                sb.append(coeff.substring(sig))
            }
            if (adj != 0L) {
                sb.append('E')
                if (adj > 0) sb.append('+')
                sb.append(adj)
            }
        }
        return sb.toString()
    }

    actual fun toEngineeringString(): String = layoutChars(false)

    actual override fun toString(): String = layoutChars(true)

    actual fun toBigInteger(): BigInteger = setScale(0, RoundingMode.DOWN).intVal

    actual fun toBigIntegerExact(): BigInteger = setScale(0, RoundingMode.UNNECESSARY).intVal

    actual fun longValueExact(): Long {
        val bi = toBigIntegerExact()
        if (bi.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 || bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw ArithmeticException("Overflow")
        }
        return bi.toLong()
    }

    actual fun intValueExact(): Int {
        val num = longValueExact()
        if (num < Int.MIN_VALUE || num > Int.MAX_VALUE) {
            throw ArithmeticException("Overflow")
        }
        return num.toInt()
    }

    actual fun shortValueExact(): Short {
        val num = longValueExact()
        if (num < Short.MIN_VALUE || num > Short.MAX_VALUE) {
            throw ArithmeticException("Overflow")
        }
        return num.toShort()
    }

    actual fun byteValueExact(): Byte {
        val num = longValueExact()
        if (num < Byte.MIN_VALUE || num > Byte.MAX_VALUE) {
            throw ArithmeticException("Overflow")
        }
        return num.toByte()
    }

    actual override fun toByte(): Byte = toInt().toByte()
    actual override fun toShort(): Short = toInt().toShort()
    actual override fun toInt(): Int = toBigInteger().toInt()
    actual override fun toLong(): Long = toBigInteger().toLong()
    actual override fun toFloat(): Float = toDouble().toFloat()
    actual override fun toDouble(): Double = toString().toDouble()
    actual override fun toChar(): Char = toInt().toChar()

    actual override fun compareTo(other: BigDecimal): Int {
        val s1 = this.signum()
        val s2 = other.signum()
        if (s1 != s2) return if (s1 > s2) 1 else -1
        if (s1 == 0) return 0
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

        actual fun valueOf(valDouble: Double): BigDecimal {
            if (valDouble.isInfinite() || valDouble.isNaN()) {
                throw NumberFormatException("Infinite or NaN")
            }
            return BigDecimal(valDouble.toString())
        }
    }
}
