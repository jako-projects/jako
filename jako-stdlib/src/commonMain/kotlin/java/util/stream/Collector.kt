@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier

expect interface Collector<T, A, R> {
    fun supplier(): Supplier<A>
    fun accumulator(): BiConsumer<A, T>
    fun combiner(): BinaryOperator<A>
    fun finisher(): Function<A, R>
    fun characteristics(): Set<Characteristics>

    enum class Characteristics {
        CONCURRENT,
        UNORDERED,
        IDENTITY_FINISH
    }

    companion object {
        fun <T, R> of(
            supplier: Supplier<R>,
            accumulator: BiConsumer<R, T>,
            combiner: BinaryOperator<R>,
            vararg characteristics: Characteristics
        ): Collector<T, R, R>

        fun <T, A, R> of(
            supplier: Supplier<A>,
            accumulator: BiConsumer<A, T>,
            combiner: BinaryOperator<A>,
            finisher: Function<A, R>,
            vararg characteristics: Characteristics
        ): Collector<T, A, R>
    }
}
