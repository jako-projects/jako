package java.util

import java.util.function.DoubleConsumer
import java.util.function.IntConsumer
import java.util.function.LongConsumer

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
