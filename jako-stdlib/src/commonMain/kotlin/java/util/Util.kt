@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.lang.Cloneable

expect open class Date : Serializable, Cloneable, Comparable<Date> {
    constructor()
    constructor(date: Long)

    open var time: Long
    open fun getTime(): Long
    open fun setTime(time: Long)
    override fun compareTo(other: Date): Int
    open fun before(whenDate: Date): Boolean
    open fun after(whenDate: Date): Boolean
    open fun toInstant(): java.time.Instant

    companion object {
        fun from(instant: java.time.Instant): Date
    }
}

expect open class Calendar : Serializable, Cloneable, Comparable<Calendar> {
    var isLenient: Boolean
    var timeInMillis: Long
    var time: Date
    var timeZone: TimeZone

    open fun get(field: Int): Int
    open fun set(field: Int, value: Int)
    fun set(year: Int, month: Int, date: Int)
    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int)
    fun set(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int, second: Int)
    fun getTime(): Date
    fun setTime(date: Date)
    fun getTimeInMillis(): Long
    fun setTimeInMillis(millis: Long)
    fun getTimeZone(): TimeZone
    fun setTimeZone(value: TimeZone)
    fun add(field: Int, amount: Int)
    override fun compareTo(other: Calendar): Int

    companion object {
        val ERA: Int
        val YEAR: Int
        val MONTH: Int
        val WEEK_OF_YEAR: Int
        val WEEK_OF_MONTH: Int
        val DATE: Int
        val DAY_OF_MONTH: Int
        val DAY_OF_YEAR: Int
        val DAY_OF_WEEK: Int
        val DAY_OF_WEEK_IN_MONTH: Int
        val AM_PM: Int
        val HOUR: Int
        val HOUR_OF_DAY: Int
        val MINUTE: Int
        val SECOND: Int
        val MILLISECOND: Int
        val ZONE_OFFSET: Int
        val DST_OFFSET: Int

        val JANUARY: Int
        val FEBRUARY: Int
        val MARCH: Int
        val APRIL: Int
        val MAY: Int
        val JUNE: Int
        val JULY: Int
        val AUGUST: Int
        val SEPTEMBER: Int
        val OCTOBER: Int
        val NOVEMBER: Int
        val DECEMBER: Int

        val SUNDAY: Int
        val MONDAY: Int
        val TUESDAY: Int
        val WEDNESDAY: Int
        val THURSDAY: Int
        val FRIDAY: Int
        val SATURDAY: Int

        fun getInstance(): Calendar
        fun getInstance(tz: TimeZone): Calendar
    }
}

expect open class GregorianCalendar : Calendar {
    constructor()
    constructor(tz: TimeZone)
    constructor(year: Int, month: Int, dayOfMonth: Int)
    constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int)
    constructor(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int, second: Int)
}

expect class UUID : Serializable, Comparable<UUID> {
    constructor(mostSigBits: Long, leastSigBits: Long)
    fun getMostSignificantBits(): Long
    fun getLeastSignificantBits(): Long
    val mostSignificantBits: Long
    val leastSignificantBits: Long
    override fun compareTo(other: UUID): Int
    override fun toString(): String

    companion object {
        fun randomUUID(): UUID
        fun nameUUIDFromBytes(name: ByteArray): UUID
        fun fromString(name: String): UUID
    }
}

expect class Base64 {
    class Encoder {
        fun encode(src: ByteArray): ByteArray
        fun encode(src: ByteArray, dst: ByteArray): Int
        fun encodeToString(src: ByteArray): String
        fun wrap(src: OutputStream): OutputStream
        fun encode(src: OutputStream): OutputStream
    }

    class Decoder {
        fun decode(src: ByteArray): ByteArray
        fun decode(src: String): ByteArray
        fun decode(src: ByteArray, dst: ByteArray): Int
        fun wrap(isStream: InputStream): InputStream
        fun decode(isStream: InputStream): InputStream
    }

    companion object {
        fun getEncoder(): Encoder
        fun getUrlEncoder(): Encoder
        fun getMimeEncoder(): Encoder
        fun getDecoder(): Decoder
        fun getUrlDecoder(): Decoder
        fun getMimeDecoder(): Decoder
    }
}

expect open class Random : Serializable {
    constructor()
    constructor(seed: Long)
    open fun nextInt(): Int
    open fun nextInt(bound: Int): Int
    open fun nextLong(): Long
    open fun nextBoolean(): Boolean
    open fun nextDouble(): Double
    open fun nextFloat(): Float
    open fun nextBytes(bytes: ByteArray)
}

expect class Objects {
    companion object {
        fun equals(a: Any?, b: Any?): Boolean
        fun hashCode(o: Any?): Int
        fun hash(vararg values: Any?): Int
        fun toString(o: Any?): String
        fun toString(o: Any?, nullDefault: String): String
        fun <T> compare(a: T?, b: T?, c: Comparator<in T>): Int
        fun <T> requireNonNull(obj: T?): T
        fun <T> requireNonNull(obj: T?, message: String): T
        fun isNull(obj: Any?): Boolean
        fun nonNull(obj: Any?): Boolean
        fun <T> requireNonNullElse(obj: T?, defaultObj: T): T
    }
}

expect open class NoSuchElementException : RuntimeException {
    constructor()
    constructor(message: String?)
}

expect interface SortedSet<E> : MutableSet<E> {
    fun comparator(): Comparator<in E>?
    fun first(): E
    fun last(): E
}

expect interface NavigableSet<E> : SortedSet<E> {
    fun descendingIterator(): MutableIterator<E>
}

expect open class TreeSet<E> : AbstractMutableSet<E>, NavigableSet<E>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in E>?)
    constructor(c: Collection<E>)
    constructor(s: SortedSet<E>)

    override val size: Int
    override fun iterator(): MutableIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    override fun add(element: E): Boolean
    override fun contains(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun clear()
    override fun comparator(): Comparator<in E>?
    override fun first(): E
    override fun last(): E
}

expect open class LinkedHashMap<K, V> : MutableMap<K, V>, Cloneable, Serializable {
    constructor(initialCapacity: Int, loadFactor: Float, accessOrder: Boolean)
    constructor(initialCapacity: Int, loadFactor: Float)
    constructor(initialCapacity: Int)
    constructor()
    constructor(m: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}

