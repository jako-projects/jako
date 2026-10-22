@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.Spliterator
import java.util.function.Supplier

expect class StreamSupport {
    companion object {
        fun <T> stream(spliterator: Spliterator<T>, parallel: Boolean): Stream<T>
        fun <T> stream(supplier: Supplier<out Spliterator<T>>, characteristics: Int, parallel: Boolean): Stream<T>
        fun intStream(spliterator: Spliterator.OfInt, parallel: Boolean): IntStream
        fun longStream(spliterator: Spliterator.OfLong, parallel: Boolean): LongStream
        fun doubleStream(spliterator: Spliterator.OfDouble, parallel: Boolean): DoubleStream
    }
}
