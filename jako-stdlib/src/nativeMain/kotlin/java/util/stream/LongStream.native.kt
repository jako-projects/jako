package java.util.stream

import java.lang.Runnable
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

actual interface LongStream : BaseStream<Long, LongStream> {
    actual fun filter(predicate: LongPredicate): LongStream
    actual fun map(mapper: LongUnaryOperator): LongStream
    actual fun <U> mapToObj(mapper: LongFunction<out U>): Stream<U>
    actual fun mapToInt(mapper: LongToIntFunction): IntStream
    actual fun mapToDouble(mapper: LongToDoubleFunction): DoubleStream
    actual fun flatMap(mapper: LongFunction<out LongStream?>): LongStream
    actual fun distinct(): LongStream
    actual fun sorted(): LongStream
    actual fun peek(action: LongConsumer): LongStream
    actual fun limit(maxSize: Long): LongStream
    actual fun skip(n: Long): LongStream
    actual fun forEach(action: LongConsumer)
    actual fun forEachOrdered(action: LongConsumer)
    actual fun toArray(): LongArray
    actual fun reduce(identity: Long, op: LongBinaryOperator): Long
    actual fun reduce(op: LongBinaryOperator): OptionalLong
    actual fun <R> collect(supplier: Supplier<R>, accumulator: ObjLongConsumer<R>, combiner: BiConsumer<R, R>): R
    actual fun sum(): Long
    actual fun min(): OptionalLong
    actual fun max(): OptionalLong
    actual fun count(): Long
    actual fun average(): OptionalDouble
    actual fun summaryStatistics(): LongSummaryStatistics
    actual fun anyMatch(predicate: LongPredicate): Boolean
    actual fun allMatch(predicate: LongPredicate): Boolean
    actual fun noneMatch(predicate: LongPredicate): Boolean
    actual fun findFirst(): OptionalLong
    actual fun findAny(): OptionalLong
    actual fun asDoubleStream(): DoubleStream
    actual fun boxed(): Stream<Long>
    actual override fun sequential(): LongStream
    actual override fun parallel(): LongStream
    actual override fun iterator(): PrimitiveIterator.OfLong
    actual override fun spliterator(): Spliterator.OfLong

    actual interface Builder : LongConsumer {
        actual override fun accept(t: Long)
        actual fun add(t: Long): Builder
        actual fun build(): LongStream
    }

    actual companion object {
        actual fun builder(): Builder = object : Builder {
            private val items = mutableListOf<Long>()
            private var built = false
            override fun accept(t: Long) {
                if (built) throw IllegalStateException("Builder already built")
                items.add(t)
            }
            override fun add(t: Long): Builder {
                accept(t)
                return this
            }
            override fun build(): LongStream {
                if (built) throw IllegalStateException("Builder already built")
                built = true
                return NativeLongStream(items.asSequence())
            }
        }

        actual fun empty(): LongStream = NativeLongStream(emptySequence())

        actual fun of(t: Long): LongStream = NativeLongStream(sequenceOf(t))

        actual fun of(vararg values: Long): LongStream = NativeLongStream(values.asSequence())

        actual fun iterate(seed: Long, f: LongUnaryOperator): LongStream =
            NativeLongStream(sequence {
                var curr = seed
                while (true) {
                    yield(curr)
                    curr = f.applyAsLong(curr)
                }
            })

        actual fun generate(s: LongSupplier): LongStream =
            NativeLongStream(sequence {
                while (true) yield(s.getAsLong())
            })

        actual fun range(startInclusive: Long, endExclusive: Long): LongStream =
            if (startInclusive >= endExclusive) empty()
            else NativeLongStream((startInclusive until endExclusive).asSequence())

        actual fun rangeClosed(startInclusive: Long, endInclusive: Long): LongStream =
            if (startInclusive > endInclusive) empty()
            else NativeLongStream((startInclusive..endInclusive).asSequence())

        actual fun concat(a: LongStream, b: LongStream): LongStream {
            val state = StreamState()
            state.closeHandlers.add(Runnable {
                try {
                    a.close()
                } finally {
                    b.close()
                }
            })
            return NativeLongStream(
                sequence {
                    val iterA = a.iterator()
                    while (iterA.hasNext()) yield(iterA.nextLong())
                    val iterB = b.iterator()
                    while (iterB.hasNext()) yield(iterB.nextLong())
                },
                state
            )
        }
    }
}
