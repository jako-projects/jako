@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

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
