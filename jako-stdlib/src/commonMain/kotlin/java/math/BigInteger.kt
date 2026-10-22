@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable
import java.util.Random

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
