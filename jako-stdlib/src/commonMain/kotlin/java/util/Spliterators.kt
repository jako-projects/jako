@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

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
