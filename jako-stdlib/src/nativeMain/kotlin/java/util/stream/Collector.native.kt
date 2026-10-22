package java.util.stream

import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier

internal class CollectorImpl<T, A, R>(
    private val supplierFn: Supplier<A>,
    private val accumulatorFn: BiConsumer<A, T>,
    private val combinerFn: BinaryOperator<A>,
    private val finisherFn: Function<A, R>,
    private val chars: Set<Collector.Characteristics>
) : Collector<T, A, R> {
    override fun supplier(): Supplier<A> = supplierFn
    override fun accumulator(): BiConsumer<A, T> = accumulatorFn
    override fun combiner(): BinaryOperator<A> = combinerFn
    override fun finisher(): Function<A, R> = finisherFn
    override fun characteristics(): Set<Collector.Characteristics> = chars
}

actual interface Collector<T, A, R> {
    actual fun supplier(): Supplier<A>
    actual fun accumulator(): BiConsumer<A, T>
    actual fun combiner(): BinaryOperator<A>
    actual fun finisher(): Function<A, R>
    actual fun characteristics(): Set<Characteristics>

    actual enum class Characteristics {
        CONCURRENT,
        UNORDERED,
        IDENTITY_FINISH
    }

    actual companion object {
        actual fun <T, R> of(
            supplier: Supplier<R>,
            accumulator: BiConsumer<R, T>,
            combiner: BinaryOperator<R>,
            vararg characteristics: Characteristics
        ): Collector<T, R, R> {
            val set = mutableSetOf(Characteristics.IDENTITY_FINISH)
            set.addAll(characteristics)
            return CollectorImpl(supplier, accumulator, combiner, Function { it }, set)
        }

        actual fun <T, A, R> of(
            supplier: Supplier<A>,
            accumulator: BiConsumer<A, T>,
            combiner: BinaryOperator<A>,
            finisher: Function<A, R>,
            vararg characteristics: Characteristics
        ): Collector<T, A, R> {
            return CollectorImpl(supplier, accumulator, combiner, finisher, characteristics.toSet())
        }
    }
}
