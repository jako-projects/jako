@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.util.LongSummaryStatistics
import java.util.OptionalDouble
import java.util.OptionalLong
import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.function.BiConsumer
import java.util.function.LongBinaryOperator
import java.util.function.LongConsumer
import java.util.function.LongFunction
import java.util.function.LongPredicate
import java.util.function.LongSupplier
import java.util.function.LongToDoubleFunction
import java.util.function.LongToIntFunction
import java.util.function.LongUnaryOperator
import java.util.function.ObjLongConsumer
import java.util.function.Supplier

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
