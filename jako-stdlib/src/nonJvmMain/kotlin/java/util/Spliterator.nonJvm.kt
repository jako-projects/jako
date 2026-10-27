package java.util

import java.util.function.DoubleConsumer
import java.util.function.IntConsumer
import java.util.function.LongConsumer

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
