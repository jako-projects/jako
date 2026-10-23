@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.DoubleSummaryStatistics
import java.util.IntSummaryStatistics
import java.util.LongSummaryStatistics
import java.util.Optional
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongFunction

expect class Collectors {
    companion object {
        fun <T, C : MutableCollection<T>> toCollection(collectionFactory: Supplier<C>): Collector<T, *, C>
        fun <T> toList(): Collector<T, *, MutableList<T>>
        fun <T> toSet(): Collector<T, *, MutableSet<T>>
        fun joining(): Collector<CharSequence, *, String>
        fun joining(delimiter: CharSequence): Collector<CharSequence, *, String>
        fun joining(delimiter: CharSequence, prefix: CharSequence, suffix: CharSequence): Collector<CharSequence, *, String>
        fun <T, U, A, R> mapping(mapper: Function<in T, out U>, downstream: Collector<in U, A, R>): Collector<T, *, R>
        fun <T, A, R, RR> collectingAndThen(downstream: Collector<T, A, R>, finisher: Function<R, RR>): Collector<T, A, RR>
        fun <T> counting(): Collector<T, *, Long>
        fun <T> minBy(comparator: Comparator<in T>): Collector<T, *, Optional<T>>
        fun <T> maxBy(comparator: Comparator<in T>): Collector<T, *, Optional<T>>
        fun <T> summingInt(mapper: ToIntFunction<in T>): Collector<T, *, Int>
        fun <T> summingLong(mapper: ToLongFunction<in T>): Collector<T, *, Long>
        fun <T> summingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, Double>
        fun <T> averagingInt(mapper: ToIntFunction<in T>): Collector<T, *, Double>
        fun <T> averagingLong(mapper: ToLongFunction<in T>): Collector<T, *, Double>
        fun <T> averagingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, Double>
        fun <T> reducing(identity: T, op: BinaryOperator<T>): Collector<T, *, T>
        fun <T> reducing(op: BinaryOperator<T>): Collector<T, *, Optional<T>>
        fun <T, U> reducing(identity: U, mapper: Function<in T, out U>, op: BinaryOperator<U>): Collector<T, *, U>
        fun <T, K> groupingBy(classifier: Function<in T, out K>): Collector<T, *, MutableMap<K, MutableList<T>>>
        fun <T, K, A, D> groupingBy(classifier: Function<in T, out K>, downstream: Collector<in T, A, D>): Collector<T, *, MutableMap<K, D>>
        fun <T, K, D, A, M : MutableMap<K, D>> groupingBy(
            classifier: Function<in T, out K>,
            mapFactory: Supplier<M>,
            downstream: Collector<in T, A, D>
        ): Collector<T, *, M>
        fun <T> partitioningBy(predicate: Predicate<in T>): Collector<T, *, MutableMap<Boolean, MutableList<T>>>
        fun <T, D, A> partitioningBy(predicate: Predicate<in T>, downstream: Collector<in T, A, D>): Collector<T, *, MutableMap<Boolean, D>>
        fun <T, K, U> toMap(keyMapper: Function<in T, out K>, valueMapper: Function<in T, out U>): Collector<T, *, MutableMap<K, U>>
        fun <T, K, U> toMap(
            keyMapper: Function<in T, out K>,
            valueMapper: Function<in T, out U>,
            mergeFunction: BinaryOperator<U>
        ): Collector<T, *, MutableMap<K, U>>
        fun <T, K, U, M : MutableMap<K, U>> toMap(
            keyMapper: Function<in T, out K>,
            valueMapper: Function<in T, out U>,
            mergeFunction: BinaryOperator<U>,
            mapSupplier: Supplier<M>
        ): Collector<T, *, M>
        fun <T> summarizingInt(mapper: ToIntFunction<in T>): Collector<T, *, IntSummaryStatistics>
        fun <T> summarizingLong(mapper: ToLongFunction<in T>): Collector<T, *, LongSummaryStatistics>
        fun <T> summarizingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, DoubleSummaryStatistics>
    }
}
