@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable
import java.util.Random

expect enum class RoundingMode {
    UP,
    DOWN,
    CEILING,
    FLOOR,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN,
    UNNECESSARY;

    companion object {
        fun valueOf(rm: Int): RoundingMode
    }
}

expect class MathContext : Serializable {
    val precision: Int
    val roundingMode: RoundingMode

    constructor(precision: Int)
    constructor(precision: Int, setRoundingMode: RoundingMode)
    constructor(valStr: String)

    companion object {
        val UNLIMITED: MathContext
        val DECIMAL32: MathContext
        val DECIMAL64: MathContext
        val DECIMAL128: MathContext
    }
}

expect class BigInteger : Number, Comparable<BigInteger>, Serializable {
    constructor(valBytes: ByteArray)
    constructor(valStr: String)
    constructor(valStr: String, radix: Int)
    constructor(signum: Int, magnitude: ByteArray)
    constructor(bitLength: Int, rnd: Random)
    constructor(bitLength: Int, certainty: Int, rnd: Random)

    fun add(valOther: BigInteger): BigInteger
    fun subtract(valOther: BigInteger): BigInteger
    fun multiply(valOther: BigInteger): BigInteger
    fun divide(valOther: BigInteger): BigInteger
    fun remainder(valOther: BigInteger): BigInteger
    fun divideAndRemainder(valOther: BigInteger): Array<BigInteger>
    fun pow(exponent: Int): BigInteger
    fun gcd(valOther: BigInteger): BigInteger
    fun abs(): BigInteger
    fun negate(): BigInteger
    fun signum(): Int
    fun mod(m: BigInteger): BigInteger
    fun modPow(exponent: BigInteger, m: BigInteger): BigInteger
    fun modInverse(m: BigInteger): BigInteger
    fun shiftLeft(n: Int): BigInteger
    fun shiftRight(n: Int): BigInteger
    fun and(valOther: BigInteger): BigInteger
    fun or(valOther: BigInteger): BigInteger
    fun xor(valOther: BigInteger): BigInteger
    fun not(): BigInteger
    fun testBit(n: Int): Boolean
    fun setBit(n: Int): BigInteger
    fun clearBit(n: Int): BigInteger
    fun flipBit(n: Int): BigInteger
    fun bitLength(): Int
    fun bitCount(): Int
    fun isProbablePrime(certainty: Int): Boolean
    fun nextProbablePrime(): BigInteger
    fun toByteArray(): ByteArray
    fun toString(radix: Int): String

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
    override fun toChar(): Char
    override fun compareTo(other: BigInteger): Int
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        val ZERO: BigInteger
        val ONE: BigInteger
        val TEN: BigInteger
        fun valueOf(valLong: Long): BigInteger
        fun probablePrime(bitLength: Int, rnd: Random): BigInteger
    }
}

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
