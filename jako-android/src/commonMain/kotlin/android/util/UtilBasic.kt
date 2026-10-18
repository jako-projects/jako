@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.util

import java.util.regex.Pattern

/**
 * Android `android.util.Log` API declaration for Kotlin Multiplatform.
 *
 * [Nullability Decision]:
 * - `tag`: nullable `String?` (Android Log allows null tag).
 * - `msg`: non-null `String` for standard `v/d/i/w/e(tag, msg)`, nullable `String?` for overloads with `Throwable?` and `wtf`.
 * - `tr`: nullable `Throwable?` (except `wtf(tag, tr)` where `tr` is non-null).
 * - `getStackTraceString(tr: Throwable?)`: returns non-null `String` (`""` when `tr == null`).
 */
expect class Log {
    companion object {
        val VERBOSE: Int
        val DEBUG: Int
        val INFO: Int
        val WARN: Int
        val ERROR: Int
        val ASSERT: Int

        fun v(tag: String?, msg: String): Int
        fun v(tag: String?, msg: String?, tr: Throwable?): Int
        fun d(tag: String?, msg: String): Int
        fun d(tag: String?, msg: String?, tr: Throwable?): Int
        fun i(tag: String?, msg: String): Int
        fun i(tag: String?, msg: String?, tr: Throwable?): Int
        fun w(tag: String?, msg: String): Int
        fun w(tag: String?, msg: String?, tr: Throwable?): Int
        fun w(tag: String?, tr: Throwable?): Int
        fun e(tag: String?, msg: String): Int
        fun e(tag: String?, msg: String?, tr: Throwable?): Int
        fun wtf(tag: String?, msg: String?): Int
        fun wtf(tag: String?, tr: Throwable): Int
        fun wtf(tag: String?, msg: String?, tr: Throwable?): Int
        fun getStackTraceString(tr: Throwable?): String
        fun println(priority: Int, tag: String?, msg: String): Int
        fun isLoggable(tag: String?, level: Int): Boolean
    }
}

/**
 * Container to ease passing around a tuple of two objects.
 *
 * [Nullability Decision]:
 * - Type parameters `F` and `S` have upper bound `Any?` to support nullable elements matching AOSP `Pair<F, S>`.
 */
expect open class Pair<F, S>(first: F, second: S) {
    val first: F
    val second: S

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun <A, B> create(a: A, b: B): Pair<A, B>
    }
}

/**
 * Immutable class for describing width and height dimensions in pixels.
 *
 * [Nullability Decision]:
 * - `parseSize(string: String)`: non-null `String` parameter, throws `NumberFormatException` on invalid format.
 */
expect final class Size(width: Int, height: Int) {
    val width: Int
    val height: Int

    fun getWidth(): Int
    fun getHeight(): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun parseSize(string: String): Size
    }
}

/**
 * Immutable class for describing width and height dimensions in floating-point values.
 */
expect final class SizeF(width: Float, height: Float) {
    val width: Float
    val height: Float

    fun getWidth(): Float
    fun getHeight(): Float
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun parseSizeF(string: String): SizeF
    }
}

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

/**
 * Immutable class for describing the range of two numeric values.
 */
expect final class Range<T : Comparable<T>>(lower: T, upper: T) {
    val lower: T
    val upper: T

    fun getLower(): T
    fun getUpper(): T
    operator fun contains(value: T): Boolean
    operator fun contains(range: Range<T>): Boolean
    fun clamp(value: T): T
    fun intersect(range: Range<T>): Range<T>
    fun intersect(lower: T, upper: T): Range<T>
    fun extend(range: Range<T>): Range<T>
    fun extend(lower: T, upper: T): Range<T>
    fun extend(value: T): Range<T>
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun <T : Comparable<T>> create(lower: T, upper: T): Range<T>
    }
}

/**
 * Utilities for encoding and decoding the Base64 representation of binary data.
 */
expect class Base64 {
    companion object {
        val DEFAULT: Int
        val NO_PADDING: Int
        val NO_WRAP: Int
        val CRLF: Int
        val URL_SAFE: Int
        val NO_CLOSE: Int

        fun decode(str: String, flags: Int): ByteArray
        fun decode(input: ByteArray, flags: Int): ByteArray
        fun decode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray
        fun encodeToString(input: ByteArray, flags: Int): String
        fun encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String
        fun encode(input: ByteArray, flags: Int): ByteArray
        fun encode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray
    }
}

/**
 * Commonly used regular expression patterns.
 */
expect class Patterns {
    companion object {
        val IP_ADDRESS: Pattern
        val DOMAIN_NAME: Pattern
        val WEB_URL: Pattern
        val EMAIL_ADDRESS: Pattern
        val PHONE: Pattern
    }
}
