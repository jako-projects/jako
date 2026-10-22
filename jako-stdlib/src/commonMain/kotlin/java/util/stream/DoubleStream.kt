@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.DoubleSummaryStatistics
import java.util.OptionalDouble
import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.function.BiConsumer
import java.util.function.DoubleBinaryOperator
import java.util.function.DoubleConsumer
import java.util.function.DoubleFunction
import java.util.function.DoublePredicate
import java.util.function.DoubleSupplier
import java.util.function.DoubleToIntFunction
import java.util.function.DoubleToLongFunction
import java.util.function.DoubleUnaryOperator
import java.util.function.ObjDoubleConsumer
import java.util.function.Supplier

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
