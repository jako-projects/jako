@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable

expect class BigDecimal : Number, Comparable<BigDecimal>, Serializable {
    constructor(valStr: String)
    constructor(valStr: String, mc: MathContext)
    constructor(valDouble: Double)
    constructor(valDouble: Double, mc: MathContext)
    constructor(valLong: Long)
    constructor(valLong: Long, mc: MathContext)
    constructor(valInt: Int)
    constructor(valInt: Int, mc: MathContext)
    constructor(valBigInt: BigInteger)
    constructor(valBigInt: BigInteger, mc: MathContext)
    constructor(unscaledVal: BigInteger, scale: Int)
    constructor(unscaledVal: BigInteger, scale: Int, mc: MathContext)

    fun add(augend: BigDecimal): BigDecimal
    fun add(augend: BigDecimal, mc: MathContext): BigDecimal
    fun subtract(subtrahend: BigDecimal): BigDecimal
    fun subtract(subtrahend: BigDecimal, mc: MathContext): BigDecimal
    fun multiply(multiplicand: BigDecimal): BigDecimal
    fun multiply(multiplicand: BigDecimal, mc: MathContext): BigDecimal
    fun divide(divisor: BigDecimal): BigDecimal
    fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal
    fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal
    fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal
    fun divideToIntegralValue(divisor: BigDecimal): BigDecimal
    fun divideToIntegralValue(divisor: BigDecimal, mc: MathContext): BigDecimal
    fun remainder(divisor: BigDecimal): BigDecimal
    fun remainder(divisor: BigDecimal, mc: MathContext): BigDecimal
    fun divideAndRemainder(divisor: BigDecimal): Array<BigDecimal>
    fun divideAndRemainder(divisor: BigDecimal, mc: MathContext): Array<BigDecimal>
    fun pow(n: Int): BigDecimal
    fun pow(n: Int, mc: MathContext): BigDecimal
    fun abs(): BigDecimal
    fun abs(mc: MathContext): BigDecimal
    fun negate(): BigDecimal
    fun negate(mc: MathContext): BigDecimal
    fun plus(): BigDecimal
    fun plus(mc: MathContext): BigDecimal
    fun round(mc: MathContext): BigDecimal
    fun signum(): Int
    fun scale(): Int
    fun precision(): Int
    fun unscaledValue(): BigInteger
    fun setScale(newScale: Int): BigDecimal
    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal
    fun stripTrailingZeros(): BigDecimal
    fun movePointLeft(n: Int): BigDecimal
    fun movePointRight(n: Int): BigDecimal
    fun scaleByPowerOfTen(n: Int): BigDecimal
    fun ulp(): BigDecimal
    fun min(valOther: BigDecimal): BigDecimal
    fun max(valOther: BigDecimal): BigDecimal
    fun toPlainString(): String
    fun toEngineeringString(): String
    fun toBigInteger(): BigInteger
    fun toBigIntegerExact(): BigInteger
    fun longValueExact(): Long
    fun intValueExact(): Int
    fun shortValueExact(): Short
    fun byteValueExact(): Byte

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
    override fun toChar(): Char
    override fun compareTo(other: BigDecimal): Int
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        val ZERO: BigDecimal
        val ONE: BigDecimal
        val TEN: BigDecimal
        fun valueOf(valLong: Long): BigDecimal
        fun valueOf(unscaledVal: Long, scale: Int): BigDecimal
        fun valueOf(valDouble: Double): BigDecimal
    }
}
