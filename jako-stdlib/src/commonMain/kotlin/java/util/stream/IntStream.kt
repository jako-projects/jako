@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.IntSummaryStatistics
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.function.BiConsumer
import java.util.function.IntBinaryOperator
import java.util.function.IntConsumer
import java.util.function.IntFunction
import java.util.function.IntPredicate
import java.util.function.IntSupplier
import java.util.function.IntToDoubleFunction
import java.util.function.IntToLongFunction
import java.util.function.IntUnaryOperator
import java.util.function.ObjIntConsumer
import java.util.function.Supplier

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
