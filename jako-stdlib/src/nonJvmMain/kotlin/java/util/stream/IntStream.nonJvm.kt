package java.util.stream

import java.lang.Runnable
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

actual interface IntStream : BaseStream<Int, IntStream> {
    actual fun filter(predicate: IntPredicate): IntStream
    actual fun map(mapper: IntUnaryOperator): IntStream
    actual fun <U> mapToObj(mapper: IntFunction<out U>): Stream<U>
    actual fun mapToLong(mapper: IntToLongFunction): LongStream
    actual fun mapToDouble(mapper: IntToDoubleFunction): DoubleStream
    actual fun flatMap(mapper: IntFunction<out IntStream?>): IntStream
    actual fun distinct(): IntStream
    actual fun sorted(): IntStream
    actual fun peek(action: IntConsumer): IntStream
    actual fun limit(maxSize: Long): IntStream
    actual fun skip(n: Long): IntStream
    actual fun forEach(action: IntConsumer)
    actual fun forEachOrdered(action: IntConsumer)
    actual fun toArray(): IntArray
    actual fun reduce(identity: Int, op: IntBinaryOperator): Int
    actual fun reduce(op: IntBinaryOperator): OptionalInt
    actual fun <R> collect(supplier: Supplier<R>, accumulator: ObjIntConsumer<R>, combiner: BiConsumer<R, R>): R
    actual fun sum(): Int
    actual fun min(): OptionalInt
    actual fun max(): OptionalInt
    actual fun count(): Long
    actual fun average(): OptionalDouble
    actual fun summaryStatistics(): IntSummaryStatistics
    actual fun anyMatch(predicate: IntPredicate): Boolean
    actual fun allMatch(predicate: IntPredicate): Boolean
    actual fun noneMatch(predicate: IntPredicate): Boolean
    actual fun findFirst(): OptionalInt
    actual fun findAny(): OptionalInt
    actual fun asLongStream(): LongStream
    actual fun asDoubleStream(): DoubleStream
    actual fun boxed(): Stream<Int>
    actual override fun sequential(): IntStream
    actual override fun parallel(): IntStream
    actual override fun iterator(): PrimitiveIterator.OfInt
    actual override fun spliterator(): Spliterator.OfInt

    actual interface Builder : IntConsumer {
        actual override fun accept(t: Int)
        actual fun add(t: Int): Builder
        actual fun build(): IntStream
    }

    actual companion object {
        actual fun builder(): Builder = object : Builder {
            private val items = mutableListOf<Int>()
            private var built = false
            override fun accept(t: Int) {
                if (built) throw IllegalStateException("Builder already built")
                items.add(t)
            }
            override fun add(t: Int): Builder {
                accept(t)
                return this
            }
            override fun build(): IntStream {
                if (built) throw IllegalStateException("Builder already built")
                built = true
                return NativeIntStream(items.asSequence())
            }
        }

        actual fun empty(): IntStream = NativeIntStream(emptySequence())

        actual fun of(t: Int): IntStream = NativeIntStream(sequenceOf(t))

        actual fun of(vararg values: Int): IntStream = NativeIntStream(values.asSequence())

        actual fun iterate(seed: Int, f: IntUnaryOperator): IntStream =
            NativeIntStream(sequence {
                var curr = seed
                while (true) {
                    yield(curr)
                    curr = f.applyAsInt(curr)
                }
            })

        actual fun generate(s: IntSupplier): IntStream =
            NativeIntStream(sequence {
                while (true) yield(s.getAsInt())
            })

        actual fun range(startInclusive: Int, endExclusive: Int): IntStream =
            if (startInclusive >= endExclusive) empty()
            else NativeIntStream((startInclusive until endExclusive).asSequence())

        actual fun rangeClosed(startInclusive: Int, endInclusive: Int): IntStream =
            if (startInclusive > endInclusive) empty()
            else NativeIntStream((startInclusive..endInclusive).asSequence())

        actual fun concat(a: IntStream, b: IntStream): IntStream {
            val state = StreamState()
            state.closeHandlers.add(Runnable {
                try {
                    a.close()
                } finally {
                    b.close()
                }
            })
            return NativeIntStream(
                sequence {
                    val iterA = a.iterator()
                    while (iterA.hasNext()) yield(iterA.nextInt())
                    val iterB = b.iterator()
                    while (iterB.hasNext()) yield(iterB.nextInt())
                },
                state
            )
        }
    }
}
