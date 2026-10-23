@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

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
