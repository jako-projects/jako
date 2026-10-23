@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable

expect class BigDecimal : Number, Comparable<BigDecimal>, Serializable {
    constructor(valStr: String)
    constructor(valDouble: Double)
    constructor(valLong: Long)
    constructor(valInt: Int)
    constructor(valBigInt: BigInteger)
    constructor(unscaledVal: BigInteger, scale: Int)

    fun add(augend: BigDecimal): BigDecimal
    fun subtract(subtrahend: BigDecimal): BigDecimal
    fun multiply(multiplicand: BigDecimal): BigDecimal
    fun divide(divisor: BigDecimal): BigDecimal
    fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal
    fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal
    fun remainder(divisor: BigDecimal): BigDecimal
    fun pow(n: Int): BigDecimal
    fun abs(): BigDecimal
    fun negate(): BigDecimal
    fun signum(): Int
    fun scale(): Int
    fun precision(): Int
    fun unscaledValue(): BigInteger
    fun setScale(newScale: Int): BigDecimal
    fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal
    fun stripTrailingZeros(): BigDecimal
    fun movePointLeft(n: Int): BigDecimal
    fun movePointRight(n: Int): BigDecimal
    fun toPlainString(): String
    fun toEngineeringString(): String

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
