package java.util.stream

import java.lang.Runnable
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

actual interface DoubleStream : BaseStream<Double, DoubleStream> {
    actual fun filter(predicate: DoublePredicate): DoubleStream
    actual fun map(mapper: DoubleUnaryOperator): DoubleStream
    actual fun <U> mapToObj(mapper: DoubleFunction<out U>): Stream<U>
    actual fun mapToInt(mapper: DoubleToIntFunction): IntStream
    actual fun mapToLong(mapper: DoubleToLongFunction): LongStream
    actual fun flatMap(mapper: DoubleFunction<out DoubleStream?>): DoubleStream
    actual fun distinct(): DoubleStream
    actual fun sorted(): DoubleStream
    actual fun peek(action: DoubleConsumer): DoubleStream
    actual fun limit(maxSize: Long): DoubleStream
    actual fun skip(n: Long): DoubleStream
    actual fun forEach(action: DoubleConsumer)
    actual fun forEachOrdered(action: DoubleConsumer)
    actual fun toArray(): DoubleArray
    actual fun reduce(identity: Double, op: DoubleBinaryOperator): Double
    actual fun reduce(op: DoubleBinaryOperator): OptionalDouble
    actual fun <R> collect(supplier: Supplier<R>, accumulator: ObjDoubleConsumer<R>, combiner: BiConsumer<R, R>): R
    actual fun sum(): Double
    actual fun min(): OptionalDouble
    actual fun max(): OptionalDouble
    actual fun count(): Long
    actual fun average(): OptionalDouble
    actual fun summaryStatistics(): DoubleSummaryStatistics
    actual fun anyMatch(predicate: DoublePredicate): Boolean
    actual fun allMatch(predicate: DoublePredicate): Boolean
    actual fun noneMatch(predicate: DoublePredicate): Boolean
    actual fun findFirst(): OptionalDouble
    actual fun findAny(): OptionalDouble
    actual fun boxed(): Stream<Double>
    actual override fun sequential(): DoubleStream
    actual override fun parallel(): DoubleStream
    actual override fun iterator(): PrimitiveIterator.OfDouble
    actual override fun spliterator(): Spliterator.OfDouble

    actual interface Builder : DoubleConsumer {
        actual override fun accept(t: Double)
        actual fun add(t: Double): Builder
        actual fun build(): DoubleStream
    }

    actual companion object {
        actual fun builder(): Builder = object : Builder {
            private val items = mutableListOf<Double>()
            private var built = false
            override fun accept(t: Double) {
                if (built) throw IllegalStateException("Builder already built")
                items.add(t)
            }
            override fun add(t: Double): Builder {
                accept(t)
                return this
            }
            override fun build(): DoubleStream {
                if (built) throw IllegalStateException("Builder already built")
                built = true
                return NativeDoubleStream(items.asSequence())
            }
        }

        actual fun empty(): DoubleStream = NativeDoubleStream(emptySequence())

        actual fun of(t: Double): DoubleStream = NativeDoubleStream(sequenceOf(t))

        actual fun of(vararg values: Double): DoubleStream = NativeDoubleStream(values.asSequence())

        actual fun iterate(seed: Double, f: DoubleUnaryOperator): DoubleStream =
            NativeDoubleStream(sequence {
                var curr = seed
                while (true) {
                    yield(curr)
                    curr = f.applyAsDouble(curr)
                }
            })

        actual fun generate(s: DoubleSupplier): DoubleStream =
            NativeDoubleStream(sequence {
                while (true) yield(s.getAsDouble())
            })

        actual fun concat(a: DoubleStream, b: DoubleStream): DoubleStream {
            val state = StreamState()
            state.closeHandlers.add(Runnable {
                try {
                    a.close()
                } finally {
                    b.close()
                }
            })
            return NativeDoubleStream(
                sequence {
                    val iterA = a.iterator()
                    while (iterA.hasNext()) yield(iterA.nextDouble())
                    val iterB = b.iterator()
                    while (iterB.hasNext()) yield(iterB.nextDouble())
                },
                state
            )
        }
    }
}
