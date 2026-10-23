package java.util

import java.util.function.DoubleConsumer
import java.util.function.IntConsumer
import java.util.function.LongConsumer

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
