package java.util

import java.io.Serializable
import java.lang.Cloneable
import java.util.function.DoubleConsumer
import java.util.function.IntConsumer
import java.util.function.LongConsumer

actual abstract class Dictionary<K, V> actual constructor() {
    actual abstract fun isEmpty(): Boolean
    actual abstract fun keys(): Enumeration<K>
    actual abstract fun elements(): Enumeration<V>
    actual abstract operator fun get(key: K): V?
    actual abstract fun put(key: K, value: V): V?
    actual abstract fun remove(key: K): V?
}

actual open class Hashtable<K, V> : Dictionary<K, V>, MutableMap<K, V>, Cloneable, Serializable {
    private val map: LinkedHashMap<K, V>

    actual constructor(initialCapacity: Int, loadFactor: Float) : super() {
        if (initialCapacity < 0) throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        if (loadFactor <= 0f || loadFactor.isNaN()) throw IllegalArgumentException("Illegal Load: $loadFactor")
        this.map = LinkedHashMap(initialCapacity, loadFactor)
    }

    actual constructor(initialCapacity: Int) : this(initialCapacity, 0.75f)

    actual constructor() : this(11, 0.75f)

    actual constructor(t: Map<out K, V>) : this(maxOf(2 * t.size, 11), 0.75f) {
        putAll(t)
    }

    actual override val size: Int
        get() = map.size

    actual override fun isEmpty(): Boolean = map.isEmpty()

    actual override fun keys(): Enumeration<K> = Collections.enumeration(map.keys.toList())

    actual override fun elements(): Enumeration<V> = Collections.enumeration(map.values.toList())

    actual open fun contains(value: Any?): Boolean {
        if (value == null) throw NullPointerException()
        @Suppress("UNCHECKED_CAST")
        return map.containsValue(value as V)
    }

    actual override fun containsValue(value: V): Boolean = contains(value)

    actual override fun containsKey(key: K): Boolean {
        if (key == null) throw NullPointerException()
        return map.containsKey(key)
    }

    actual override fun get(key: K): V? {
        if (key == null) throw NullPointerException()
        return map[key]
    }

    actual override fun put(key: K, value: V): V? {
        if (key == null || value == null) throw NullPointerException()
        return map.put(key, value)
    }

    actual override fun remove(key: K): V? {
        if (key == null) throw NullPointerException()
        return map.remove(key)
    }

    actual override fun putAll(from: Map<out K, V>) {
        for ((k, v) in from) {
            put(k, v)
        }
    }

    actual override fun clear() {
        map.clear()
    }

    actual open fun clone(): Any {
        val copy = Hashtable<K, V>()
        copy.map.putAll(this.map)
        return copy
    }

    actual override val keys: MutableSet<K>
        get() = map.keys

    actual override val values: MutableCollection<V>
        get() = map.values

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries

    override fun equals(other: Any?): Boolean = map == other

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}

actual open class Vector<E> : AbstractMutableList<E>, MutableList<E>, RandomAccess, Cloneable, Serializable {
    private val list: ArrayList<E>
    private var cap: Int
    private val capIncrement: Int

    actual constructor(initialCapacity: Int, capacityIncrement: Int) : super() {
        if (initialCapacity < 0) throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        this.list = ArrayList(initialCapacity)
        this.cap = initialCapacity
        this.capIncrement = capacityIncrement
    }

    actual constructor(initialCapacity: Int) : this(initialCapacity, 0)

    actual constructor() : this(10, 0)

    actual constructor(c: Collection<E>) : super() {
        this.list = ArrayList(c)
        this.cap = c.size
        this.capIncrement = 0
    }

    private fun growCapacityIfNeeded(minCap: Int) {
        if (minCap > cap) {
            val next = if (capIncrement > 0) cap + capIncrement else if (cap == 0) 10 else cap * 2
            cap = maxOf(minCap, next)
        }
    }

    actual open fun copyInto(anArray: Array<Any?>) {
        for (i in 0 until list.size) {
            anArray[i] = list[i]
        }
    }

    actual open fun trimToSize() {
        list.trimToSize()
        cap = list.size
    }

    actual open fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > 0) {
            list.ensureCapacity(minCapacity)
            growCapacityIfNeeded(minCapacity)
        }
    }

    actual open fun setSize(newSize: Int) {
        if (newSize < 0) throw IndexOutOfBoundsException("newSize < 0: $newSize")
        if (newSize > list.size) {
            growCapacityIfNeeded(newSize)
            @Suppress("UNCHECKED_CAST")
            while (list.size < newSize) {
                list.add(null as E)
            }
        } else {
            while (list.size > newSize) {
                list.removeAt(list.size - 1)
            }
        }
    }

    actual open fun capacity(): Int = maxOf(cap, list.size)

    actual override val size: Int
        get() = list.size

    actual override fun isEmpty(): Boolean = list.isEmpty()

    actual open fun elements(): Enumeration<E> {
        return object : Enumeration<E> {
            var count = 0
            override fun hasMoreElements(): Boolean = count < list.size
            override fun nextElement(): E {
                if (count < list.size) {
                    return list[count++]
                }
                throw NoSuchElementException("Vector Enumeration")
            }
        }
    }

    actual override fun contains(element: E): Boolean = list.contains(element)

    actual override fun indexOf(element: E): Int = list.indexOf(element)

    actual open fun indexOf(o: Any?, index: Int): Int {
        if (index < 0) throw IndexOutOfBoundsException("$index < 0")
        for (i in index until list.size) {
            if (list[i] == o) return i
        }
        return -1
    }

    actual override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    actual open fun lastIndexOf(o: Any?, index: Int): Int {
        if (index >= list.size) throw IndexOutOfBoundsException("$index >= ${list.size}")
        for (i in index downTo 0) {
            if (list[i] == o) return i
        }
        return -1
    }

    actual open fun elementAt(index: Int): E = list[index]

    actual open fun firstElement(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[0]
    }

    actual open fun lastElement(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[list.size - 1]
    }

    actual open fun setElementAt(obj: E, index: Int) {
        list[index] = obj
    }

    actual open fun removeElementAt(index: Int) {
        list.removeAt(index)
    }

    actual open fun insertElementAt(obj: E, index: Int) {
        growCapacityIfNeeded(list.size + 1)
        list.add(index, obj)
    }

    actual open fun addElement(obj: E) {
        growCapacityIfNeeded(list.size + 1)
        list.add(obj)
    }

    actual open fun removeElement(obj: Any?): Boolean {
        val idx = indexOf(obj, 0)
        if (idx >= 0) {
            removeElementAt(idx)
            return true
        }
        return false
    }

    actual open fun removeAllElements() {
        list.clear()
    }

    actual open fun clone(): Any {
        val copy = Vector<E>(capacity(), capIncrement)
        copy.list.addAll(this.list)
        return copy
    }

    actual override fun get(index: Int): E = list[index]

    actual override fun set(index: Int, element: E): E = list.set(index, element)

    actual override fun add(element: E): Boolean {
        growCapacityIfNeeded(list.size + 1)
        return list.add(element)
    }

    actual override fun remove(element: E): Boolean = list.remove(element)

    actual override fun add(index: Int, element: E) {
        growCapacityIfNeeded(list.size + 1)
        list.add(index, element)
    }

    actual override fun removeAt(index: Int): E = list.removeAt(index)

    actual override fun clear() {
        list.clear()
    }

    actual override fun addAll(elements: Collection<E>): Boolean {
        growCapacityIfNeeded(list.size + elements.size)
        return list.addAll(elements)
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        growCapacityIfNeeded(list.size + elements.size)
        return list.addAll(index, elements)
    }

    actual override fun iterator(): MutableIterator<E> = list.iterator()

    actual override fun listIterator(): MutableListIterator<E> = list.listIterator()

    actual override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    actual override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.subList(fromIndex, toIndex)
}

actual open class Stack<E> actual constructor() : Vector<E>() {
    actual open fun push(item: E): E {
        addElement(item)
        return item
    }

    actual open fun pop(): E {
        val len = size
        val obj = peek()
        removeElementAt(len - 1)
        return obj
    }

    actual open fun peek(): E {
        val len = size
        if (len == 0) throw EmptyStackException()
        return elementAt(len - 1)
    }

    actual open fun empty(): Boolean = size == 0

    actual open fun search(o: Any?): Int {
        val i = lastIndexOf(o, size - 1)
        return if (i >= 0) size - i else -1
    }
}

actual abstract class AbstractSequentialList<E> protected actual constructor() : AbstractMutableList<E>() {
    actual override fun get(index: Int): E {
        try {
            return listIterator(index).next()
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun set(index: Int, element: E): E {
        try {
            val e = listIterator(index)
            val oldVal = e.next()
            e.set(element)
            return oldVal
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun add(index: Int, element: E) {
        try {
            listIterator(index).add(element)
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun removeAt(index: Int): E {
        try {
            val out = listIterator(index)
            val outCast = out.next()
            out.remove()
            return outCast
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean {
        try {
            var modified = false
            val e1 = listIterator(index)
            for (e in elements) {
                e1.add(e)
                modified = true
            }
            return modified
        } catch (_: NoSuchElementException) {
            throw IndexOutOfBoundsException("Index: $index")
        }
    }

    actual override fun iterator(): MutableIterator<E> = listIterator()

    actual abstract override fun listIterator(index: Int): MutableListIterator<E>
}

actual open class LinkedList<E> : AbstractSequentialList<E>, MutableList<E>, Deque<E>, Cloneable, Serializable {
    private val list = ArrayList<E>()

    actual constructor() : super()

    actual constructor(c: Collection<E>) : super() {
        list.addAll(c)
    }

    actual override val size: Int
        get() = list.size

    actual override fun getFirst(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[0]
    }

    actual override fun getLast(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list[list.size - 1]
    }

    actual override fun removeFirst(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list.removeAt(0)
    }

    actual override fun removeLast(): E {
        if (list.isEmpty()) throw NoSuchElementException()
        return list.removeAt(list.size - 1)
    }

    actual override fun addFirst(e: E) {
        list.add(0, e)
    }

    actual override fun addLast(e: E) {
        list.add(e)
    }

    actual override fun contains(element: E): Boolean = list.contains(element)

    actual override fun add(element: E): Boolean = list.add(element)

    actual override fun remove(element: E): Boolean = list.remove(element)

    actual override fun addAll(elements: Collection<E>): Boolean = list.addAll(elements)

    actual override fun addAll(index: Int, elements: Collection<E>): Boolean = list.addAll(index, elements)

    actual override fun clear() {
        list.clear()
    }

    actual override fun get(index: Int): E = list[index]

    actual override fun set(index: Int, element: E): E = list.set(index, element)

    actual override fun add(index: Int, element: E) {
        list.add(index, element)
    }

    actual override fun removeAt(index: Int): E = list.removeAt(index)

    actual override fun indexOf(element: E): Int = list.indexOf(element)

    actual override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    actual override fun peek(): E? = if (list.isEmpty()) null else list[0]

    actual override fun poll(): E? = if (list.isEmpty()) null else list.removeAt(0)

    actual override fun offer(e: E): Boolean = add(e)

    actual override fun offerFirst(e: E): Boolean {
        addFirst(e)
        return true
    }

    actual override fun offerLast(e: E): Boolean {
        addLast(e)
        return true
    }

    actual override fun peekFirst(): E? = peek()

    actual override fun peekLast(): E? = if (list.isEmpty()) null else list[list.size - 1]

    actual override fun pollFirst(): E? = poll()

    actual override fun pollLast(): E? = if (list.isEmpty()) null else list.removeAt(list.size - 1)

    actual override fun push(e: E) {
        addFirst(e)
    }

    actual override fun pop(): E = removeFirst()

    actual open fun removeFirstOccurrence(o: Any?): Boolean {
        val idx = list.indexOf(o)
        if (idx >= 0) {
            list.removeAt(idx)
            return true
        }
        return false
    }

    actual open fun removeLastOccurrence(o: Any?): Boolean {
        val idx = list.lastIndexOf(o)
        if (idx >= 0) {
            list.removeAt(idx)
            return true
        }
        return false
    }

    actual override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    actual override fun descendingIterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            private var cursor = list.size - 1
            private var lastRet = -1

            override fun hasNext(): Boolean = cursor >= 0

            override fun next(): E {
                if (cursor < 0) throw NoSuchElementException()
                lastRet = cursor
                return list[cursor--]
            }

            override fun remove() {
                if (lastRet < 0) throw IllegalStateException()
                list.removeAt(lastRet)
                lastRet = -1
            }
        }
    }

    actual open fun clone(): Any = LinkedList(list)
}

actual abstract class EventListenerProxy<T : EventListener> actual constructor(
    listener: T
) : EventListener {
    private val listenerVal: T = listener
    actual open fun getListener(): T = listenerVal
    actual open val listener: T
        get() = listenerVal
}

actual open class IntSummaryStatistics actual constructor() : IntConsumer {
    private var countVal: Long = 0L
    private var sumVal: Long = 0L
    private var minVal: Int = Int.MAX_VALUE
    private var maxVal: Int = Int.MIN_VALUE

    actual override fun accept(value: Int) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: IntSummaryStatistics) {
        countVal += other.countVal
        sumVal += other.sumVal
        minVal = minOf(minVal, other.minVal)
        maxVal = maxOf(maxVal, other.maxVal)
    }

    actual fun getCount(): Long = countVal
    actual val count: Long
        get() = countVal

    actual fun getSum(): Long = sumVal
    actual val sum: Long
        get() = sumVal

    actual fun getMin(): Int = minVal
    actual val min: Int
        get() = minVal

    actual fun getMax(): Int = maxVal
    actual val max: Int
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal.toDouble() / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "IntSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}

actual open class LongSummaryStatistics actual constructor() : LongConsumer, IntConsumer {
    private var countVal: Long = 0L
    private var sumVal: Long = 0L
    private var minVal: Long = Long.MAX_VALUE
    private var maxVal: Long = Long.MIN_VALUE

    actual override fun accept(value: Int) {
        accept(value.toLong())
    }

    actual override fun accept(value: Long) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: LongSummaryStatistics) {
        countVal += other.countVal
        sumVal += other.sumVal
        minVal = minOf(minVal, other.minVal)
        maxVal = maxOf(maxVal, other.maxVal)
    }

    actual fun getCount(): Long = countVal
    actual val count: Long
        get() = countVal

    actual fun getSum(): Long = sumVal
    actual val sum: Long
        get() = sumVal

    actual fun getMin(): Long = minVal
    actual val min: Long
        get() = minVal

    actual fun getMax(): Long = maxVal
    actual val max: Long
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal.toDouble() / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "LongSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}

actual open class DoubleSummaryStatistics actual constructor() : DoubleConsumer {
    private var countVal: Long = 0L
    private var sumVal: Double = 0.0
    private var minVal: Double = Double.POSITIVE_INFINITY
    private var maxVal: Double = Double.NEGATIVE_INFINITY

    actual override fun accept(value: Double) {
        ++countVal
        sumVal += value
        minVal = minOf(minVal, value)
        maxVal = maxOf(maxVal, value)
    }

    actual open fun combine(other: DoubleSummaryStatistics) {
        countVal += other.countVal
        sumVal += other.sumVal
        minVal = minOf(minVal, other.minVal)
        maxVal = maxOf(maxVal, other.maxVal)
    }

    actual fun getCount(): Long = countVal
    actual val count: Long
        get() = countVal

    actual fun getSum(): Double = sumVal
    actual val sum: Double
        get() = sumVal

    actual fun getMin(): Double = minVal
    actual val min: Double
        get() = minVal

    actual fun getMax(): Double = maxVal
    actual val max: Double
        get() = maxVal

    actual fun getAverage(): Double = if (countVal > 0) sumVal / countVal else 0.0
    actual val average: Double
        get() = getAverage()

    actual override fun toString(): String {
        return "DoubleSummaryStatistics{count=$countVal, sum=$sumVal, min=$minVal, average=${getAverage()}, max=$maxVal}"
    }
}

actual interface PrimitiveIterator<T, T_CONS> : MutableIterator<T> {
    actual fun forEachRemaining(action: T_CONS)

    actual interface OfInt : PrimitiveIterator<Int, IntConsumer> {
        actual fun nextInt(): Int
        actual override fun next(): Int
        actual override fun forEachRemaining(action: IntConsumer)
    }

    actual interface OfLong : PrimitiveIterator<Long, LongConsumer> {
        actual fun nextLong(): Long
        actual override fun next(): Long
        actual override fun forEachRemaining(action: LongConsumer)
    }

    actual interface OfDouble : PrimitiveIterator<Double, DoubleConsumer> {
        actual fun nextDouble(): Double
        actual override fun next(): Double
        actual override fun forEachRemaining(action: DoubleConsumer)
    }
}

actual interface Spliterator<T> {
    actual fun tryAdvance(action: java.util.function.Consumer<in T>): Boolean
    actual fun forEachRemaining(action: java.util.function.Consumer<in T>)
    actual fun trySplit(): Spliterator<T>?
    actual fun estimateSize(): Long
    actual fun getExactSizeIfKnown(): Long
    actual fun characteristics(): Int
    actual fun hasCharacteristics(characteristics: Int): Boolean

    actual interface OfPrimitive<T, T_CONS, T_SPLITR : OfPrimitive<T, T_CONS, T_SPLITR>> : Spliterator<T> {
        actual override fun trySplit(): T_SPLITR?
        actual fun tryAdvance(action: T_CONS): Boolean
        actual fun forEachRemaining(action: T_CONS)
    }

    actual interface OfInt : OfPrimitive<Int, IntConsumer, OfInt> {
        actual override fun trySplit(): OfInt?
        actual override fun tryAdvance(action: IntConsumer): Boolean
        actual override fun forEachRemaining(action: IntConsumer)
    }

    actual interface OfLong : OfPrimitive<Long, LongConsumer, OfLong> {
        actual override fun trySplit(): OfLong?
        actual override fun tryAdvance(action: LongConsumer): Boolean
        actual override fun forEachRemaining(action: LongConsumer)
    }

    actual interface OfDouble : OfPrimitive<Double, DoubleConsumer, OfDouble> {
        actual override fun trySplit(): OfDouble?
        actual override fun tryAdvance(action: DoubleConsumer): Boolean
        actual override fun forEachRemaining(action: DoubleConsumer)
    }

    actual companion object {
        actual val ORDERED: Int = 0x00000010
        actual val DISTINCT: Int = 0x00000001
        actual val SORTED: Int = 0x00000004
        actual val SIZED: Int = 0x00000040
        actual val NONNULL: Int = 0x00000100
        actual val IMMUTABLE: Int = 0x00000400
        actual val CONCURRENT: Int = 0x00001000
        actual val SUBSIZED: Int = 0x00004000
    }
}

internal class ListSpliterator<T>(
    private val list: List<T>,
    private var index: Int = 0,
    private val fence: Int = list.size,
    private val chars: Int = Spliterator.ORDERED or Spliterator.SIZED or Spliterator.SUBSIZED
) : Spliterator<T> {
    override fun tryAdvance(action: java.util.function.Consumer<in T>): Boolean {
        if (index < fence) {
            action.accept(list[index++])
            return true
        }
        return false
    }

    override fun forEachRemaining(action: java.util.function.Consumer<in T>) {
        while (index < fence) {
            action.accept(list[index++])
        }
    }

    override fun trySplit(): Spliterator<T>? {
        val lo = index
        val mid = (lo + fence) ushr 1
        return if (lo >= mid) null else {
            index = mid
            ListSpliterator(list, lo, mid, chars)
        }
    }

    override fun estimateSize(): Long = (fence - index).toLong()

    override fun getExactSizeIfKnown(): Long =
        if ((characteristics() and Spliterator.SIZED) == 0) -1L else estimateSize()

    override fun characteristics(): Int = chars

    override fun hasCharacteristics(characteristics: Int): Boolean =
        (characteristics() and characteristics) == characteristics
}

internal class IntArraySpliterator(
    private val array: IntArray,
    private var index: Int = 0,
    private val fence: Int = array.size,
    private val chars: Int = Spliterator.ORDERED or Spliterator.SIZED or Spliterator.SUBSIZED or Spliterator.IMMUTABLE
) : Spliterator.OfInt {
    override fun tryAdvance(action: IntConsumer): Boolean {
        if (index < fence) {
            action.accept(array[index++])
            return true
        }
        return false
    }

    override fun forEachRemaining(action: IntConsumer) {
        while (index < fence) {
            action.accept(array[index++])
        }
    }

    override fun tryAdvance(action: java.util.function.Consumer<in Int>): Boolean =
        tryAdvance(IntConsumer { action.accept(it) })

    override fun forEachRemaining(action: java.util.function.Consumer<in Int>) =
        forEachRemaining(IntConsumer { action.accept(it) })

    override fun trySplit(): Spliterator.OfInt? {
        val lo = index
        val mid = (lo + fence) ushr 1
        return if (lo >= mid) null else {
            index = mid
            IntArraySpliterator(array, lo, mid, chars)
        }
    }

    override fun estimateSize(): Long = (fence - index).toLong()

    override fun getExactSizeIfKnown(): Long =
        if ((characteristics() and Spliterator.SIZED) == 0) -1L else estimateSize()

    override fun characteristics(): Int = chars

    override fun hasCharacteristics(characteristics: Int): Boolean =
        (characteristics() and characteristics) == characteristics
}

internal class LongArraySpliterator(
    private val array: LongArray,
    private var index: Int = 0,
    private val fence: Int = array.size,
    private val chars: Int = Spliterator.ORDERED or Spliterator.SIZED or Spliterator.SUBSIZED or Spliterator.IMMUTABLE
) : Spliterator.OfLong {
    override fun tryAdvance(action: LongConsumer): Boolean {
        if (index < fence) {
            action.accept(array[index++])
            return true
        }
        return false
    }

    override fun forEachRemaining(action: LongConsumer) {
        while (index < fence) {
            action.accept(array[index++])
        }
    }

    override fun tryAdvance(action: java.util.function.Consumer<in Long>): Boolean =
        tryAdvance(LongConsumer { action.accept(it) })

    override fun forEachRemaining(action: java.util.function.Consumer<in Long>) =
        forEachRemaining(LongConsumer { action.accept(it) })

    override fun trySplit(): Spliterator.OfLong? {
        val lo = index
        val mid = (lo + fence) ushr 1
        return if (lo >= mid) null else {
            index = mid
            LongArraySpliterator(array, lo, mid, chars)
        }
    }

    override fun estimateSize(): Long = (fence - index).toLong()

    override fun getExactSizeIfKnown(): Long =
        if ((characteristics() and Spliterator.SIZED) == 0) -1L else estimateSize()

    override fun characteristics(): Int = chars

    override fun hasCharacteristics(characteristics: Int): Boolean =
        (characteristics() and characteristics) == characteristics
}

internal class DoubleArraySpliterator(
    private val array: DoubleArray,
    private var index: Int = 0,
    private val fence: Int = array.size,
    private val chars: Int = Spliterator.ORDERED or Spliterator.SIZED or Spliterator.SUBSIZED or Spliterator.IMMUTABLE
) : Spliterator.OfDouble {
    override fun tryAdvance(action: DoubleConsumer): Boolean {
        if (index < fence) {
            action.accept(array[index++])
            return true
        }
        return false
    }

    override fun forEachRemaining(action: DoubleConsumer) {
        while (index < fence) {
            action.accept(array[index++])
        }
    }

    override fun tryAdvance(action: java.util.function.Consumer<in Double>): Boolean =
        tryAdvance(DoubleConsumer { action.accept(it) })

    override fun forEachRemaining(action: java.util.function.Consumer<in Double>) =
        forEachRemaining(DoubleConsumer { action.accept(it) })

    override fun trySplit(): Spliterator.OfDouble? {
        val lo = index
        val mid = (lo + fence) ushr 1
        return if (lo >= mid) null else {
            index = mid
            DoubleArraySpliterator(array, lo, mid, chars)
        }
    }

    override fun estimateSize(): Long = (fence - index).toLong()

    override fun getExactSizeIfKnown(): Long =
        if ((characteristics() and Spliterator.SIZED) == 0) -1L else estimateSize()

    override fun characteristics(): Int = chars

    override fun hasCharacteristics(characteristics: Int): Boolean =
        (characteristics() and characteristics) == characteristics
}

actual class Spliterators private constructor() {
    actual companion object {
        actual fun <T> emptySpliterator(): Spliterator<T> = ListSpliterator(emptyList())

        actual fun emptyIntSpliterator(): Spliterator.OfInt = IntArraySpliterator(IntArray(0))

        actual fun emptyLongSpliterator(): Spliterator.OfLong = LongArraySpliterator(LongArray(0))

        actual fun emptyDoubleSpliterator(): Spliterator.OfDouble = DoubleArraySpliterator(DoubleArray(0))

        actual fun <T> spliterator(c: Collection<out T>, characteristics: Int): Spliterator<T> {
            return ListSpliterator(c.toList(), 0, c.size, characteristics or Spliterator.SIZED or Spliterator.SUBSIZED)
        }

        actual fun <T> spliteratorUnknownSize(iterator: Iterator<out T>, characteristics: Int): Spliterator<T> {
            return object : Spliterator<T> {
                override fun tryAdvance(action: java.util.function.Consumer<in T>): Boolean {
                    if (iterator.hasNext()) {
                        action.accept(iterator.next())
                        return true
                    }
                    return false
                }
                override fun forEachRemaining(action: java.util.function.Consumer<in T>) {
                    while (iterator.hasNext()) {
                        action.accept(iterator.next())
                    }
                }
                override fun trySplit(): Spliterator<T>? = null
                override fun estimateSize(): Long = Long.MAX_VALUE
                override fun getExactSizeIfKnown(): Long = -1L
                override fun characteristics(): Int = characteristics and (Spliterator.SIZED or Spliterator.SUBSIZED).inv()
                override fun hasCharacteristics(characteristics: Int): Boolean =
                    (characteristics() and characteristics) == characteristics
            }
        }

        actual fun <T> iterator(spliterator: Spliterator<out T>): Iterator<T> {
            return object : Iterator<T> {
                private var valueReady = false
                private var nextElement: T? = null

                override fun hasNext(): Boolean {
                    if (!valueReady) {
                        spliterator.tryAdvance { t ->
                            valueReady = true
                            nextElement = t
                        }
                    }
                    return valueReady
                }

                override fun next(): T {
                    if (!valueReady && !hasNext()) throw NoSuchElementException()
                    valueReady = false
                    @Suppress("UNCHECKED_CAST")
                    return nextElement as T
                }
            }
        }

        actual fun iterator(spliterator: Spliterator.OfInt): PrimitiveIterator.OfInt {
            return object : PrimitiveIterator.OfInt {
                private var valueReady = false
                private var nextElement = 0

                override fun hasNext(): Boolean {
                    if (!valueReady) {
                        spliterator.tryAdvance(IntConsumer { v ->
                            valueReady = true
                            nextElement = v
                        })
                    }
                    return valueReady
                }

                override fun nextInt(): Int {
                    if (!valueReady && !hasNext()) throw NoSuchElementException()
                    valueReady = false
                    return nextElement
                }

                override fun next(): Int = nextInt()

                override fun forEachRemaining(action: IntConsumer) {
                    while (hasNext()) {
                        action.accept(nextInt())
                    }
                }

                override fun remove() {
                    throw UnsupportedOperationException("remove")
                }
            }
        }

        actual fun iterator(spliterator: Spliterator.OfLong): PrimitiveIterator.OfLong {
            return object : PrimitiveIterator.OfLong {
                private var valueReady = false
                private var nextElement = 0L

                override fun hasNext(): Boolean {
                    if (!valueReady) {
                        spliterator.tryAdvance(LongConsumer { v ->
                            valueReady = true
                            nextElement = v
                        })
                    }
                    return valueReady
                }

                override fun nextLong(): Long {
                    if (!valueReady && !hasNext()) throw NoSuchElementException()
                    valueReady = false
                    return nextElement
                }

                override fun next(): Long = nextLong()

                override fun forEachRemaining(action: LongConsumer) {
                    while (hasNext()) {
                        action.accept(nextLong())
                    }
                }

                override fun remove() {
                    throw UnsupportedOperationException("remove")
                }
            }
        }

        actual fun iterator(spliterator: Spliterator.OfDouble): PrimitiveIterator.OfDouble {
            return object : PrimitiveIterator.OfDouble {
                private var valueReady = false
                private var nextElement = 0.0

                override fun hasNext(): Boolean {
                    if (!valueReady) {
                        spliterator.tryAdvance(DoubleConsumer { v ->
                            valueReady = true
                            nextElement = v
                        })
                    }
                    return valueReady
                }

                override fun nextDouble(): Double {
                    if (!valueReady && !hasNext()) throw NoSuchElementException()
                    valueReady = false
                    return nextElement
                }

                override fun next(): Double = nextDouble()

                override fun forEachRemaining(action: DoubleConsumer) {
                    while (hasNext()) {
                        action.accept(nextDouble())
                    }
                }

                override fun remove() {
                    throw UnsupportedOperationException("remove")
                }
            }
        }
    }
}

