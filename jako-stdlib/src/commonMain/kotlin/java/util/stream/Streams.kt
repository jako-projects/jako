@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.lang.Runnable
import java.util.DoubleSummaryStatistics
import java.util.IntSummaryStatistics
import java.util.LongSummaryStatistics
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong
import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.DoubleBinaryOperator
import java.util.function.DoubleConsumer
import java.util.function.DoubleFunction
import java.util.function.DoublePredicate
import java.util.function.DoubleSupplier
import java.util.function.DoubleToIntFunction
import java.util.function.DoubleToLongFunction
import java.util.function.DoubleUnaryOperator
import java.util.function.Function
import java.util.function.IntBinaryOperator
import java.util.function.IntConsumer
import java.util.function.IntFunction
import java.util.function.IntPredicate
import java.util.function.IntSupplier
import java.util.function.IntToDoubleFunction
import java.util.function.IntToLongFunction
import java.util.function.IntUnaryOperator
import java.util.function.LongBinaryOperator
import java.util.function.LongConsumer
import java.util.function.LongFunction
import java.util.function.LongPredicate
import java.util.function.LongSupplier
import java.util.function.LongToDoubleFunction
import java.util.function.LongToIntFunction
import java.util.function.LongUnaryOperator
import java.util.function.ObjDoubleConsumer
import java.util.function.ObjIntConsumer
import java.util.function.ObjLongConsumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongFunction
import java.util.function.UnaryOperator

expect interface BaseStream<T, S : BaseStream<T, S>> : AutoCloseable {
    fun iterator(): MutableIterator<T>
    fun spliterator(): Spliterator<T>
    fun isParallel(): Boolean
    fun sequential(): S
    fun parallel(): S
    fun unordered(): S
    fun onClose(closeHandler: Runnable): S
    override fun close()
}

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

expect interface IntStream : BaseStream<Int, IntStream> {
    fun filter(predicate: IntPredicate): IntStream
    fun map(mapper: IntUnaryOperator): IntStream
    fun <U> mapToObj(mapper: IntFunction<out U>): Stream<U>
    fun mapToLong(mapper: IntToLongFunction): LongStream
    fun mapToDouble(mapper: IntToDoubleFunction): DoubleStream
    fun flatMap(mapper: IntFunction<out IntStream?>): IntStream
    fun distinct(): IntStream
    fun sorted(): IntStream
    fun peek(action: IntConsumer): IntStream
    fun limit(maxSize: Long): IntStream
    fun skip(n: Long): IntStream
    fun forEach(action: IntConsumer)
    fun forEachOrdered(action: IntConsumer)
    fun toArray(): IntArray
    fun reduce(identity: Int, op: IntBinaryOperator): Int
    fun reduce(op: IntBinaryOperator): OptionalInt
    fun <R> collect(supplier: Supplier<R>, accumulator: ObjIntConsumer<R>, combiner: BiConsumer<R, R>): R
    fun sum(): Int
    fun min(): OptionalInt
    fun max(): OptionalInt
    fun count(): Long
    fun average(): OptionalDouble
    fun summaryStatistics(): IntSummaryStatistics
    fun anyMatch(predicate: IntPredicate): Boolean
    fun allMatch(predicate: IntPredicate): Boolean
    fun noneMatch(predicate: IntPredicate): Boolean
    fun findFirst(): OptionalInt
    fun findAny(): OptionalInt
    fun asLongStream(): LongStream
    fun asDoubleStream(): DoubleStream
    fun boxed(): Stream<Int>
    override fun sequential(): IntStream
    override fun parallel(): IntStream
    override fun iterator(): PrimitiveIterator.OfInt
    override fun spliterator(): Spliterator.OfInt

    interface Builder : IntConsumer {
        override fun accept(t: Int)
        fun add(t: Int): Builder
        fun build(): IntStream
    }

    companion object {
        fun builder(): Builder
        fun empty(): IntStream
        fun of(t: Int): IntStream
        fun of(vararg values: Int): IntStream
        fun iterate(seed: Int, f: IntUnaryOperator): IntStream
        fun generate(s: IntSupplier): IntStream
        fun range(startInclusive: Int, endExclusive: Int): IntStream
        fun rangeClosed(startInclusive: Int, endInclusive: Int): IntStream
        fun concat(a: IntStream, b: IntStream): IntStream
    }
}

expect interface LongStream : BaseStream<Long, LongStream> {
    fun filter(predicate: LongPredicate): LongStream
    fun map(mapper: LongUnaryOperator): LongStream
    fun <U> mapToObj(mapper: LongFunction<out U>): Stream<U>
    fun mapToInt(mapper: LongToIntFunction): IntStream
    fun mapToDouble(mapper: LongToDoubleFunction): DoubleStream
    fun flatMap(mapper: LongFunction<out LongStream?>): LongStream
    fun distinct(): LongStream
    fun sorted(): LongStream
    fun peek(action: LongConsumer): LongStream
    fun limit(maxSize: Long): LongStream
    fun skip(n: Long): LongStream
    fun forEach(action: LongConsumer)
    fun forEachOrdered(action: LongConsumer)
    fun toArray(): LongArray
    fun reduce(identity: Long, op: LongBinaryOperator): Long
    fun reduce(op: LongBinaryOperator): OptionalLong
    fun <R> collect(supplier: Supplier<R>, accumulator: ObjLongConsumer<R>, combiner: BiConsumer<R, R>): R
    fun sum(): Long
    fun min(): OptionalLong
    fun max(): OptionalLong
    fun count(): Long
    fun average(): OptionalDouble
    fun summaryStatistics(): LongSummaryStatistics
    fun anyMatch(predicate: LongPredicate): Boolean
    fun allMatch(predicate: LongPredicate): Boolean
    fun noneMatch(predicate: LongPredicate): Boolean
    fun findFirst(): OptionalLong
    fun findAny(): OptionalLong
    fun asDoubleStream(): DoubleStream
    fun boxed(): Stream<Long>
    override fun sequential(): LongStream
    override fun parallel(): LongStream
    override fun iterator(): PrimitiveIterator.OfLong
    override fun spliterator(): Spliterator.OfLong

    interface Builder : LongConsumer {
        override fun accept(t: Long)
        fun add(t: Long): Builder
        fun build(): LongStream
    }

    companion object {
        fun builder(): Builder
        fun empty(): LongStream
        fun of(t: Long): LongStream
        fun of(vararg values: Long): LongStream
        fun iterate(seed: Long, f: LongUnaryOperator): LongStream
        fun generate(s: LongSupplier): LongStream
        fun range(startInclusive: Long, endExclusive: Long): LongStream
        fun rangeClosed(startInclusive: Long, endInclusive: Long): LongStream
        fun concat(a: LongStream, b: LongStream): LongStream
    }
}

expect interface DoubleStream : BaseStream<Double, DoubleStream> {
    fun filter(predicate: DoublePredicate): DoubleStream
    fun map(mapper: DoubleUnaryOperator): DoubleStream
    fun <U> mapToObj(mapper: DoubleFunction<out U>): Stream<U>
    fun mapToInt(mapper: DoubleToIntFunction): IntStream
    fun mapToLong(mapper: DoubleToLongFunction): LongStream
    fun flatMap(mapper: DoubleFunction<out DoubleStream?>): DoubleStream
    fun distinct(): DoubleStream
    fun sorted(): DoubleStream
    fun peek(action: DoubleConsumer): DoubleStream
    fun limit(maxSize: Long): DoubleStream
    fun skip(n: Long): DoubleStream
    fun forEach(action: DoubleConsumer)
    fun forEachOrdered(action: DoubleConsumer)
    fun toArray(): DoubleArray
    fun reduce(identity: Double, op: DoubleBinaryOperator): Double
    fun reduce(op: DoubleBinaryOperator): OptionalDouble
    fun <R> collect(supplier: Supplier<R>, accumulator: ObjDoubleConsumer<R>, combiner: BiConsumer<R, R>): R
    fun sum(): Double
    fun min(): OptionalDouble
    fun max(): OptionalDouble
    fun count(): Long
    fun average(): OptionalDouble
    fun summaryStatistics(): DoubleSummaryStatistics
    fun anyMatch(predicate: DoublePredicate): Boolean
    fun allMatch(predicate: DoublePredicate): Boolean
    fun noneMatch(predicate: DoublePredicate): Boolean
    fun findFirst(): OptionalDouble
    fun findAny(): OptionalDouble
    fun boxed(): Stream<Double>
    override fun sequential(): DoubleStream
    override fun parallel(): DoubleStream
    override fun iterator(): PrimitiveIterator.OfDouble
    override fun spliterator(): Spliterator.OfDouble

    interface Builder : DoubleConsumer {
        override fun accept(t: Double)
        fun add(t: Double): Builder
        fun build(): DoubleStream
    }

    companion object {
        fun builder(): Builder
        fun empty(): DoubleStream
        fun of(t: Double): DoubleStream
        fun of(vararg values: Double): DoubleStream
        fun iterate(seed: Double, f: DoubleUnaryOperator): DoubleStream
        fun generate(s: DoubleSupplier): DoubleStream
        fun concat(a: DoubleStream, b: DoubleStream): DoubleStream
    }
}

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

expect class StreamSupport {
    companion object {
        fun <T> stream(spliterator: Spliterator<T>, parallel: Boolean): Stream<T>
        fun <T> stream(supplier: Supplier<out Spliterator<T>>, characteristics: Int, parallel: Boolean): Stream<T>
        fun intStream(spliterator: Spliterator.OfInt, parallel: Boolean): IntStream
        fun longStream(spliterator: Spliterator.OfLong, parallel: Boolean): LongStream
        fun doubleStream(spliterator: Spliterator.OfDouble, parallel: Boolean): DoubleStream
    }
}
