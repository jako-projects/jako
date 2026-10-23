@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

/**
 * An immutable data type representation a rational number.
 */
expect final class Rational(numerator: Int, denominator: Int) : Number, Comparable<Rational> {
    val numerator: Int
    val denominator: Int
    val isNaN: Boolean
    val isInfinite: Boolean
    val isFinite: Boolean
    val isZero: Boolean

    fun getNumerator(): Int
    fun getDenominator(): Int
    fun isNaN(): Boolean
    fun isInfinite(): Boolean
    fun isFinite(): Boolean
    fun isZero(): Boolean

    override fun toByte(): Byte
    override fun toShort(): Short
    override fun toInt(): Int
    override fun toLong(): Long
    override fun toFloat(): Float
    override fun toDouble(): Double
    @Suppress("DEPRECATION")
    override fun toChar(): Char

    override fun compareTo(other: Rational): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        val NaN: Rational
        val POSITIVE_INFINITY: Rational
        val NEGATIVE_INFINITY: Rational
        val ZERO: Rational

        fun parseRational(string: String): Rational
    }
}
