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
    open fun setSeed(seed: Long)
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
        fun deepEquals(a: Any?, b: Any?): Boolean
        fun hashCode(o: Any?): Int
        fun hash(vararg values: Any?): Int
        fun toString(o: Any?): String
        fun toString(o: Any?, nullDefault: String): String
        fun <T> compare(a: T?, b: T?, c: Comparator<in T>): Int
        fun <T> requireNonNull(obj: T?): T
        fun <T> requireNonNull(obj: T?, message: String): T
        fun <T> requireNonNull(obj: T?, messageSupplier: java.util.function.Supplier<String>): T
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

expect abstract class Dictionary<K, V>() {
    abstract fun isEmpty(): Boolean
    abstract fun keys(): Enumeration<K>
    abstract fun elements(): Enumeration<V>
    abstract operator fun get(key: K): V?
    abstract fun put(key: K, value: V): V?
    abstract fun remove(key: K): V?
}

expect open class Hashtable<K, V> : Dictionary<K, V>, MutableMap<K, V>, Cloneable, Serializable {
    constructor(initialCapacity: Int, loadFactor: Float)
    constructor(initialCapacity: Int)
    constructor()
    constructor(t: Map<out K, V>)

    override val size: Int
    override fun isEmpty(): Boolean
    override fun keys(): Enumeration<K>
    override fun elements(): Enumeration<V>
    open fun contains(value: Any?): Boolean
    override fun containsValue(value: V): Boolean
    override fun containsKey(key: K): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
    open fun clone(): Any
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}

expect open class Vector<E> : AbstractMutableList<E>, MutableList<E>, RandomAccess, Cloneable, Serializable {
    constructor(initialCapacity: Int, capacityIncrement: Int)
    constructor(initialCapacity: Int)
    constructor()
    constructor(c: Collection<E>)

    open fun copyInto(anArray: Array<Any?>)
    open fun trimToSize()
    open fun ensureCapacity(minCapacity: Int)
    open fun setSize(newSize: Int)
    open fun capacity(): Int
    override val size: Int
    override fun isEmpty(): Boolean
    open fun elements(): Enumeration<E>
    override fun contains(element: E): Boolean
    override fun indexOf(element: E): Int
    open fun indexOf(o: Any?, index: Int): Int
    override fun lastIndexOf(element: E): Int
    open fun lastIndexOf(o: Any?, index: Int): Int
    open fun elementAt(index: Int): E
    open fun firstElement(): E
    open fun lastElement(): E
    open fun setElementAt(obj: E, index: Int)
    open fun removeElementAt(index: Int)
    open fun insertElementAt(obj: E, index: Int)
    open fun addElement(obj: E)
    open fun removeElement(obj: Any?): Boolean
    open fun removeAllElements()
    open fun clone(): Any
    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun clear()
    override fun addAll(elements: Collection<E>): Boolean
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun iterator(): MutableIterator<E>
    override fun listIterator(): MutableListIterator<E>
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
}

expect open class Stack<E>() : Vector<E> {
    open fun push(item: E): E
    open fun pop(): E
    open fun peek(): E
    open fun empty(): Boolean
    open fun search(o: Any?): Int
}

expect abstract class AbstractSequentialList<E> : AbstractMutableList<E> {
    protected constructor()

    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun iterator(): MutableIterator<E>
    abstract override fun listIterator(index: Int): MutableListIterator<E>
}

expect open class LinkedList<E> : AbstractSequentialList<E>, MutableList<E>, Deque<E>, Cloneable, Serializable {
    constructor()
    constructor(c: Collection<E>)

    override val size: Int
    override fun getFirst(): E
    override fun getLast(): E
    override fun removeFirst(): E
    override fun removeLast(): E
    override fun addFirst(e: E)
    override fun addLast(e: E)
    override fun contains(element: E): Boolean
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun addAll(elements: Collection<E>): Boolean
    override fun addAll(index: Int, elements: Collection<E>): Boolean
    override fun clear()
    override fun get(index: Int): E
    override fun set(index: Int, element: E): E
    override fun add(index: Int, element: E)
    override fun removeAt(index: Int): E
    override fun indexOf(element: E): Int
    override fun lastIndexOf(element: E): Int
    override fun peek(): E?
    override fun poll(): E?
    override fun offer(e: E): Boolean
    override fun offerFirst(e: E): Boolean
    override fun offerLast(e: E): Boolean
    override fun peekFirst(): E?
    override fun peekLast(): E?
    override fun pollFirst(): E?
    override fun pollLast(): E?
    override fun push(e: E)
    override fun pop(): E
    open fun removeFirstOccurrence(o: Any?): Boolean
    open fun removeLastOccurrence(o: Any?): Boolean
    override fun listIterator(index: Int): MutableListIterator<E>
    override fun descendingIterator(): MutableIterator<E>
    open fun clone(): Any
}

expect abstract class EventListenerProxy<T : EventListener>(listener: T) : EventListener {
    open fun getListener(): T
    open val listener: T
}

expect open class IntSummaryStatistics() : java.util.function.IntConsumer {
    override fun accept(value: Int)
    open fun combine(other: IntSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Long
    val sum: Long
    fun getMin(): Int
    val min: Int
    fun getMax(): Int
    val max: Int
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}

expect open class LongSummaryStatistics() : java.util.function.LongConsumer, java.util.function.IntConsumer {
    override fun accept(value: Int)
    override fun accept(value: Long)
    open fun combine(other: LongSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Long
    val sum: Long
    fun getMin(): Long
    val min: Long
    fun getMax(): Long
    val max: Long
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}

expect open class DoubleSummaryStatistics() : java.util.function.DoubleConsumer {
    override fun accept(value: Double)
    open fun combine(other: DoubleSummaryStatistics)
    fun getCount(): Long
    val count: Long
    fun getSum(): Double
    val sum: Double
    fun getMin(): Double
    val min: Double
    fun getMax(): Double
    val max: Double
    fun getAverage(): Double
    val average: Double
    override fun toString(): String
}

expect open class EmptyStackException() : RuntimeException

expect open class ConcurrentModificationException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(cause: Throwable?)
    constructor(message: String?, cause: Throwable?)
}

expect open class TooManyListenersException : Exception {
    constructor()
    constructor(s: String?)
}

expect open class MissingResourceException : RuntimeException {
    constructor(s: String?, className: String?, key: String?)

    open fun getClassName(): String?
    open val className: String?
    open fun getKey(): String?
    open val key: String?
}

expect open class InvalidPropertiesFormatException : java.io.IOException {
    constructor(cause: Throwable?)
    constructor(message: String?)
}

expect open class IllformedLocaleException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, errorIndex: Int)

    open fun getErrorIndex(): Int
    open val errorIndex: Int
}

expect interface PrimitiveIterator<T, T_CONS> : MutableIterator<T> {
    fun forEachRemaining(action: T_CONS)

    interface OfInt : PrimitiveIterator<Int, java.util.function.IntConsumer> {
        fun nextInt(): Int
        override fun next(): Int
        override fun forEachRemaining(action: java.util.function.IntConsumer)
    }

    interface OfLong : PrimitiveIterator<Long, java.util.function.LongConsumer> {
        fun nextLong(): Long
        override fun next(): Long
        override fun forEachRemaining(action: java.util.function.LongConsumer)
    }

    interface OfDouble : PrimitiveIterator<Double, java.util.function.DoubleConsumer> {
        fun nextDouble(): Double
        override fun next(): Double
        override fun forEachRemaining(action: java.util.function.DoubleConsumer)
    }
}

expect interface Spliterator<T> {
    fun tryAdvance(action: java.util.function.Consumer<in T>): Boolean
    fun forEachRemaining(action: java.util.function.Consumer<in T>)
    fun trySplit(): Spliterator<T>?
    fun estimateSize(): Long
    fun getExactSizeIfKnown(): Long
    fun characteristics(): Int
    fun hasCharacteristics(characteristics: Int): Boolean

    interface OfPrimitive<T, T_CONS, T_SPLITR : OfPrimitive<T, T_CONS, T_SPLITR>> : Spliterator<T> {
        override fun trySplit(): T_SPLITR?
        fun tryAdvance(action: T_CONS): Boolean
        fun forEachRemaining(action: T_CONS)
    }

    interface OfInt : OfPrimitive<Int, java.util.function.IntConsumer, OfInt> {
        override fun trySplit(): OfInt?
        override fun tryAdvance(action: java.util.function.IntConsumer): Boolean
        override fun forEachRemaining(action: java.util.function.IntConsumer)
    }

    interface OfLong : OfPrimitive<Long, java.util.function.LongConsumer, OfLong> {
        override fun trySplit(): OfLong?
        override fun tryAdvance(action: java.util.function.LongConsumer): Boolean
        override fun forEachRemaining(action: java.util.function.LongConsumer)
    }

    interface OfDouble : OfPrimitive<Double, java.util.function.DoubleConsumer, OfDouble> {
        override fun trySplit(): OfDouble?
        override fun tryAdvance(action: java.util.function.DoubleConsumer): Boolean
        override fun forEachRemaining(action: java.util.function.DoubleConsumer)
    }

    companion object {
        val ORDERED: Int
        val DISTINCT: Int
        val SORTED: Int
        val SIZED: Int
        val NONNULL: Int
        val IMMUTABLE: Int
        val CONCURRENT: Int
        val SUBSIZED: Int
    }
}

expect class Spliterators {
    companion object {
        fun <T> emptySpliterator(): Spliterator<T>
        fun emptyIntSpliterator(): Spliterator.OfInt
        fun emptyLongSpliterator(): Spliterator.OfLong
        fun emptyDoubleSpliterator(): Spliterator.OfDouble
        fun <T> spliterator(c: Collection<out T>, characteristics: Int): Spliterator<T>
        fun <T> spliteratorUnknownSize(iterator: Iterator<out T>, characteristics: Int): Spliterator<T>
        fun <T> iterator(spliterator: Spliterator<out T>): Iterator<T>
        fun iterator(spliterator: Spliterator.OfInt): PrimitiveIterator.OfInt
        fun iterator(spliterator: Spliterator.OfLong): PrimitiveIterator.OfLong
        fun iterator(spliterator: Spliterator.OfDouble): PrimitiveIterator.OfDouble
    }
}



