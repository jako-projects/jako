@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongFunction
import java.util.function.UnaryOperator

expect interface Stream<T> : BaseStream<T, Stream<T>> {
    fun filter(predicate: Predicate<in T>): Stream<T>
    fun <R> map(mapper: Function<in T, out R>): Stream<R>
    fun mapToInt(mapper: ToIntFunction<in T>): IntStream
    fun mapToLong(mapper: ToLongFunction<in T>): LongStream
    fun mapToDouble(mapper: ToDoubleFunction<in T>): DoubleStream
    fun <R> flatMap(mapper: Function<in T, out Stream<out R>?>): Stream<R>
    fun flatMapToInt(mapper: Function<in T, out IntStream?>): IntStream
    fun flatMapToLong(mapper: Function<in T, out LongStream?>): LongStream
    fun flatMapToDouble(mapper: Function<in T, out DoubleStream?>): DoubleStream
    fun distinct(): Stream<T>
    fun sorted(): Stream<T>
    fun sorted(comparator: Comparator<in T>): Stream<T>
    fun peek(action: Consumer<in T>): Stream<T>
    fun limit(maxSize: Long): Stream<T>
    fun skip(n: Long): Stream<T>
    fun forEach(action: Consumer<in T>)
    fun forEachOrdered(action: Consumer<in T>)
    fun toArray(): Array<Any?>
    fun reduce(identity: T, accumulator: BinaryOperator<T>): T
    fun reduce(accumulator: BinaryOperator<T>): Optional<T>
    fun <U> reduce(identity: U, accumulator: BiFunction<U, in T, U>, combiner: BinaryOperator<U>): U
    fun <R> collect(supplier: Supplier<R>, accumulator: BiConsumer<R, in T>, combiner: BiConsumer<R, R>): R
    fun <R, A> collect(collector: Collector<in T, A, R>): R
    fun min(comparator: Comparator<in T>): Optional<T>
    fun max(comparator: Comparator<in T>): Optional<T>
    fun count(): Long
    fun anyMatch(predicate: Predicate<in T>): Boolean
    fun allMatch(predicate: Predicate<in T>): Boolean
    fun noneMatch(predicate: Predicate<in T>): Boolean
    fun findFirst(): Optional<T>
    fun findAny(): Optional<T>

    interface Builder<T> : Consumer<T> {
        override fun accept(t: T)
        fun add(t: T): Builder<T>
        fun build(): Stream<T>
    }

    companion object {
        fun <T> builder(): Builder<T>
        fun <T> empty(): Stream<T>
        fun <T> of(t: T): Stream<T>
        fun <T> of(vararg values: T): Stream<T>
        fun <T> iterate(seed: T, f: UnaryOperator<T>): Stream<T>
        fun <T> generate(s: Supplier<T>): Stream<T>
        fun <T> concat(a: Stream<out T>, b: Stream<out T>): Stream<T>
    }
}
