package java.util.stream

import java.lang.Runnable
import java.util.DoubleSummaryStatistics
import java.util.IntSummaryStatistics
import java.util.LongSummaryStatistics
import java.util.NoSuchElementException
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong
import java.util.PrimitiveIterator
import java.util.Spliterator
import java.util.Spliterators
import java.util.StringJoiner
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

actual interface BaseStream<T, S : BaseStream<T, S>> : AutoCloseable {
    actual fun iterator(): MutableIterator<T>
    actual fun spliterator(): Spliterator<T>
    actual fun isParallel(): Boolean
    actual fun sequential(): S
    actual fun parallel(): S
    actual fun unordered(): S
    actual fun onClose(closeHandler: Runnable): S
    actual override fun close()
}

internal class StreamState(
    var parallel: Boolean = false,
    val closeHandlers: MutableList<Runnable> = mutableListOf(),
    var consumed: Boolean = false
) {
    fun markConsumed() {
        if (consumed) {
            throw IllegalStateException("stream has already been operated upon or closed")
        }
        consumed = true
    }

    fun closeAll() {
        consumed = true
        var firstError: Throwable? = null
        val handlers = closeHandlers.toList()
        closeHandlers.clear()
        for (h in handlers) {
            try {
                h.run()
            } catch (t: Throwable) {
                if (firstError == null) firstError = t else firstError.addSuppressed(t)
            }
        }
        if (firstError != null) throw firstError
    }
}

internal class ReferenceStream<T>(
    private val seq: Sequence<T>,
    private val state: StreamState = StreamState()
) : Stream<T> {

    private fun consumeSeq(): Sequence<T> {
        state.markConsumed()
        return seq
    }

    private fun <R> derive(newSeq: Sequence<R>): Stream<R> {
        state.markConsumed()
        return ReferenceStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    private fun deriveInt(newSeq: Sequence<Int>): IntStream {
        state.markConsumed()
        return NativeIntStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    private fun deriveLong(newSeq: Sequence<Long>): LongStream {
        state.markConsumed()
        return NativeLongStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    private fun deriveDouble(newSeq: Sequence<Double>): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun iterator(): MutableIterator<T> {
        val iter = consumeSeq().iterator()
        return object : MutableIterator<T> {
            override fun hasNext(): Boolean = iter.hasNext()
            override fun next(): T = iter.next()
            override fun remove() = throw UnsupportedOperationException("remove")
        }
    }

    override fun spliterator(): Spliterator<T> {
        return Spliterators.spliteratorUnknownSize(consumeSeq().iterator(), Spliterator.ORDERED)
    }

    override fun isParallel(): Boolean = state.parallel

    override fun sequential(): Stream<T> {
        state.parallel = false
        return this
    }

    override fun parallel(): Stream<T> {
        state.parallel = true
        return this
    }

    override fun unordered(): Stream<T> = this

    override fun onClose(closeHandler: Runnable): Stream<T> {
        if (state.consumed) throw IllegalStateException("stream has already been operated upon or closed")
        state.closeHandlers.add(closeHandler)
        return this
    }

    override fun close() {
        state.closeAll()
    }

    override fun filter(predicate: Predicate<in T>): Stream<T> =
        derive(seq.filter { predicate.test(it) })

    override fun <R> map(mapper: Function<in T, out R>): Stream<R> =
        derive(seq.map { mapper.apply(it) })

    override fun mapToInt(mapper: ToIntFunction<in T>): IntStream =
        deriveInt(seq.map { mapper.applyAsInt(it) })

    override fun mapToLong(mapper: ToLongFunction<in T>): LongStream =
        deriveLong(seq.map { mapper.applyAsLong(it) })

    override fun mapToDouble(mapper: ToDoubleFunction<in T>): DoubleStream =
        deriveDouble(seq.map { mapper.applyAsDouble(it) })

    override fun <R> flatMap(mapper: Function<in T, out Stream<out R>?>): Stream<R> =
        derive(sequence {
            for (item in seq) {
                val sub = mapper.apply(item)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) {
                            yield(iter.next())
                        }
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun flatMapToInt(mapper: Function<in T, out IntStream?>): IntStream =
        deriveInt(sequence {
            for (item in seq) {
                val sub = mapper.apply(item)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) {
                            yield(iter.nextInt())
                        }
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun flatMapToLong(mapper: Function<in T, out LongStream?>): LongStream =
        deriveLong(sequence {
            for (item in seq) {
                val sub = mapper.apply(item)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) {
                            yield(iter.nextLong())
                        }
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun flatMapToDouble(mapper: Function<in T, out DoubleStream?>): DoubleStream =
        deriveDouble(sequence {
            for (item in seq) {
                val sub = mapper.apply(item)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) {
                            yield(iter.nextDouble())
                        }
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun distinct(): Stream<T> = derive(seq.distinct())

    override fun sorted(): Stream<T> = derive(sequence {
        val list = seq.toMutableList()
        @Suppress("UNCHECKED_CAST")
        (list as MutableList<Comparable<Any>>).sort()
        yieldAll(list)
    })

    override fun sorted(comparator: Comparator<in T>): Stream<T> = derive(sequence {
        val list = seq.toMutableList()
        list.sortWith(comparator)
        yieldAll(list)
    })

    override fun peek(action: Consumer<in T>): Stream<T> =
        derive(seq.onEach { action.accept(it) })

    override fun limit(maxSize: Long): Stream<T> {
        if (maxSize < 0) throw IllegalArgumentException("maxSize < 0: $maxSize")
        return derive(sequence {
            var remaining = maxSize
            val iter = seq.iterator()
            while (remaining > 0L && iter.hasNext()) {
                yield(iter.next())
                remaining--
            }
        })
    }

    override fun skip(n: Long): Stream<T> {
        if (n < 0) throw IllegalArgumentException("n < 0: $n")
        return derive(sequence {
            var toSkip = n
            val iter = seq.iterator()
            while (toSkip > 0L && iter.hasNext()) {
                iter.next()
                toSkip--
            }
            while (iter.hasNext()) {
                yield(iter.next())
            }
        })
    }

    override fun forEach(action: Consumer<in T>) {
        for (item in consumeSeq()) {
            action.accept(item)
        }
    }

    override fun forEachOrdered(action: Consumer<in T>) = forEach(action)

    override fun toArray(): Array<Any?> {
        val list = consumeSeq().toList()
        return Array(list.size) { list[it] }
    }

    override fun reduce(identity: T, accumulator: BinaryOperator<T>): T {
        var result = identity
        for (item in consumeSeq()) {
            result = accumulator.apply(result, item)
        }
        return result
    }

    override fun reduce(accumulator: BinaryOperator<T>): Optional<T> {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return Optional.empty()
        var result = iter.next()
        while (iter.hasNext()) {
            result = accumulator.apply(result, iter.next())
        }
        if (result == null) throw NullPointerException()
        return Optional.ofNullable(result)
    }

    override fun <U> reduce(identity: U, accumulator: BiFunction<U, in T, U>, combiner: BinaryOperator<U>): U {
        var result = identity
        for (item in consumeSeq()) {
            result = accumulator.apply(result, item)
        }
        return result
    }

    override fun <R> collect(supplier: Supplier<R>, accumulator: BiConsumer<R, in T>, combiner: BiConsumer<R, R>): R {
        val container = supplier.get()
        for (item in consumeSeq()) {
            accumulator.accept(container, item)
        }
        return container
    }

    override fun <R, A> collect(collector: Collector<in T, A, R>): R {
        val container = collector.supplier().get()
        val acc = collector.accumulator()
        for (item in consumeSeq()) {
            acc.accept(container, item)
        }
        @Suppress("UNCHECKED_CAST")
        return if (collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            container as R
        } else {
            collector.finisher().apply(container)
        }
    }

    override fun min(comparator: Comparator<in T>): Optional<T> {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return Optional.empty()
        var best = iter.next()
        while (iter.hasNext()) {
            val next = iter.next()
            if (comparator.compare(next, best) < 0) best = next
        }
        if (best == null) throw NullPointerException()
        return Optional.ofNullable(best)
    }

    override fun max(comparator: Comparator<in T>): Optional<T> {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return Optional.empty()
        var best = iter.next()
        while (iter.hasNext()) {
            val next = iter.next()
            if (comparator.compare(next, best) > 0) best = next
        }
        if (best == null) throw NullPointerException()
        return Optional.ofNullable(best)
    }

    override fun count(): Long {
        var c = 0L
        for (_item in consumeSeq()) c++
        return c
    }

    override fun anyMatch(predicate: Predicate<in T>): Boolean {
        for (item in consumeSeq()) {
            if (predicate.test(item)) return true
        }
        return false
    }

    override fun allMatch(predicate: Predicate<in T>): Boolean {
        for (item in consumeSeq()) {
            if (!predicate.test(item)) return false
        }
        return true
    }

    override fun noneMatch(predicate: Predicate<in T>): Boolean {
        for (item in consumeSeq()) {
            if (predicate.test(item)) return false
        }
        return true
    }

    override fun findFirst(): Optional<T> {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return Optional.empty()
        val first = iter.next() ?: throw NullPointerException()
        return Optional.ofNullable(first)
    }

    override fun findAny(): Optional<T> = findFirst()
}

actual interface Stream<T> : BaseStream<T, Stream<T>> {
    actual fun filter(predicate: Predicate<in T>): Stream<T>
    actual fun <R> map(mapper: Function<in T, out R>): Stream<R>
    actual fun mapToInt(mapper: ToIntFunction<in T>): IntStream
    actual fun mapToLong(mapper: ToLongFunction<in T>): LongStream
    actual fun mapToDouble(mapper: ToDoubleFunction<in T>): DoubleStream
    actual fun <R> flatMap(mapper: Function<in T, out Stream<out R>?>): Stream<R>
    actual fun flatMapToInt(mapper: Function<in T, out IntStream?>): IntStream
    actual fun flatMapToLong(mapper: Function<in T, out LongStream?>): LongStream
    actual fun flatMapToDouble(mapper: Function<in T, out DoubleStream?>): DoubleStream
    actual fun distinct(): Stream<T>
    actual fun sorted(): Stream<T>
    actual fun sorted(comparator: Comparator<in T>): Stream<T>
    actual fun peek(action: Consumer<in T>): Stream<T>
    actual fun limit(maxSize: Long): Stream<T>
    actual fun skip(n: Long): Stream<T>
    actual fun forEach(action: Consumer<in T>)
    actual fun forEachOrdered(action: Consumer<in T>)
    actual fun toArray(): Array<Any?>
    actual fun reduce(identity: T, accumulator: BinaryOperator<T>): T
    actual fun reduce(accumulator: BinaryOperator<T>): Optional<T>
    actual fun <U> reduce(identity: U, accumulator: BiFunction<U, in T, U>, combiner: BinaryOperator<U>): U
    actual fun <R> collect(supplier: Supplier<R>, accumulator: BiConsumer<R, in T>, combiner: BiConsumer<R, R>): R
    actual fun <R, A> collect(collector: Collector<in T, A, R>): R
    actual fun min(comparator: Comparator<in T>): Optional<T>
    actual fun max(comparator: Comparator<in T>): Optional<T>
    actual fun count(): Long
    actual fun anyMatch(predicate: Predicate<in T>): Boolean
    actual fun allMatch(predicate: Predicate<in T>): Boolean
    actual fun noneMatch(predicate: Predicate<in T>): Boolean
    actual fun findFirst(): Optional<T>
    actual fun findAny(): Optional<T>

    actual interface Builder<T> : Consumer<T> {
        actual override fun accept(t: T)
        actual fun add(t: T): Builder<T>
        actual fun build(): Stream<T>
    }

    actual companion object {
        actual fun <T> builder(): Builder<T> = object : Builder<T> {
            private val items = mutableListOf<T>()
            private var built = false

            override fun accept(t: T) {
                if (built) throw IllegalStateException("Builder already built")
                items.add(t)
            }

            override fun add(t: T): Builder<T> {
                accept(t)
                return this
            }

            override fun build(): Stream<T> {
                if (built) throw IllegalStateException("Builder already built")
                built = true
                return ReferenceStream(items.asSequence())
            }
        }

        actual fun <T> empty(): Stream<T> = ReferenceStream(emptySequence())

        actual fun <T> of(t: T): Stream<T> = ReferenceStream(sequenceOf(t))

        actual fun <T> of(vararg values: T): Stream<T> = ReferenceStream(values.asSequence())

        actual fun <T> iterate(seed: T, f: UnaryOperator<T>): Stream<T> =
            ReferenceStream(sequence {
                var curr = seed
                while (true) {
                    yield(curr)
                    curr = f.apply(curr)
                }
            })

        actual fun <T> generate(s: Supplier<T>): Stream<T> =
            ReferenceStream(sequence {
                while (true) {
                    yield(s.get())
                }
            })

        actual fun <T> concat(a: Stream<out T>, b: Stream<out T>): Stream<T> {
            val state = StreamState()
            state.closeHandlers.add(Runnable {
                try {
                    a.close()
                } finally {
                    b.close()
                }
            })
            return ReferenceStream(
                sequence {
                    val iterA = a.iterator()
                    while (iterA.hasNext()) yield(iterA.next())
                    val iterB = b.iterator()
                    while (iterB.hasNext()) yield(iterB.next())
                },
                state
            )
        }
    }
}

internal class NativeIntStream(
    private val seq: Sequence<Int>,
    private val state: StreamState = StreamState()
) : IntStream {

    private fun consumeSeq(): Sequence<Int> {
        state.markConsumed()
        return seq
    }

    private fun derive(newSeq: Sequence<Int>): IntStream {
        state.markConsumed()
        return NativeIntStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun filter(predicate: IntPredicate): IntStream =
        derive(seq.filter { predicate.test(it) })

    override fun map(mapper: IntUnaryOperator): IntStream =
        derive(seq.map { mapper.applyAsInt(it) })

    override fun <U> mapToObj(mapper: IntFunction<out U>): Stream<U> {
        state.markConsumed()
        return ReferenceStream(seq.map { mapper.apply(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToLong(mapper: IntToLongFunction): LongStream {
        state.markConsumed()
        return NativeLongStream(seq.map { mapper.applyAsLong(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToDouble(mapper: IntToDoubleFunction): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(seq.map { mapper.applyAsDouble(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun flatMap(mapper: IntFunction<out IntStream?>): IntStream =
        derive(sequence {
            for (v in seq) {
                val sub = mapper.apply(v)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) yield(iter.nextInt())
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun distinct(): IntStream = derive(seq.distinct())

    override fun sorted(): IntStream = derive(sequence {
        val list = seq.toMutableList()
        list.sort()
        yieldAll(list)
    })

    override fun peek(action: IntConsumer): IntStream =
        derive(seq.onEach { action.accept(it) })

    override fun limit(maxSize: Long): IntStream {
        if (maxSize < 0) throw IllegalArgumentException("maxSize < 0: $maxSize")
        return derive(sequence {
            var remaining = maxSize
            val iter = seq.iterator()
            while (remaining > 0L && iter.hasNext()) {
                yield(iter.next())
                remaining--
            }
        })
    }

    override fun skip(n: Long): IntStream {
        if (n < 0) throw IllegalArgumentException("n < 0: $n")
        return derive(sequence {
            var toSkip = n
            val iter = seq.iterator()
            while (toSkip > 0L && iter.hasNext()) {
                iter.next()
                toSkip--
            }
            while (iter.hasNext()) yield(iter.next())
        })
    }

    override fun forEach(action: IntConsumer) {
        for (v in consumeSeq()) action.accept(v)
    }

    override fun forEachOrdered(action: IntConsumer) = forEach(action)

    override fun toArray(): IntArray = consumeSeq().toList().toIntArray()

    override fun reduce(identity: Int, op: IntBinaryOperator): Int {
        var res = identity
        for (v in consumeSeq()) res = op.applyAsInt(res, v)
        return res
    }

    override fun reduce(op: IntBinaryOperator): OptionalInt {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalInt.empty()
        var res = iter.next()
        while (iter.hasNext()) res = op.applyAsInt(res, iter.next())
        return OptionalInt.of(res)
    }

    override fun <R> collect(supplier: Supplier<R>, accumulator: ObjIntConsumer<R>, combiner: BiConsumer<R, R>): R {
        val c = supplier.get()
        for (v in consumeSeq()) accumulator.accept(c, v)
        return c
    }

    override fun sum(): Int {
        var s = 0
        for (v in consumeSeq()) s += v
        return s
    }

    override fun min(): OptionalInt {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalInt.empty()
        var m = iter.next()
        while (iter.hasNext()) m = minOf(m, iter.next())
        return OptionalInt.of(m)
    }

    override fun max(): OptionalInt {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalInt.empty()
        var m = iter.next()
        while (iter.hasNext()) m = maxOf(m, iter.next())
        return OptionalInt.of(m)
    }

    override fun count(): Long {
        var c = 0L
        for (_v in consumeSeq()) c++
        return c
    }

    override fun average(): OptionalDouble {
        var c = 0L
        var sum = 0L
        for (v in consumeSeq()) {
            c++
            sum += v
        }
        return if (c == 0L) OptionalDouble.empty() else OptionalDouble.of(sum.toDouble() / c)
    }

    override fun summaryStatistics(): IntSummaryStatistics {
        val stats = IntSummaryStatistics()
        for (v in consumeSeq()) stats.accept(v)
        return stats
    }

    override fun anyMatch(predicate: IntPredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return true
        return false
    }

    override fun allMatch(predicate: IntPredicate): Boolean {
        for (v in consumeSeq()) if (!predicate.test(v)) return false
        return true
    }

    override fun noneMatch(predicate: IntPredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return false
        return true
    }

    override fun findFirst(): OptionalInt {
        val iter = consumeSeq().iterator()
        return if (iter.hasNext()) OptionalInt.of(iter.next()) else OptionalInt.empty()
    }

    override fun findAny(): OptionalInt = findFirst()

    override fun asLongStream(): LongStream {
        state.markConsumed()
        return NativeLongStream(seq.map { it.toLong() }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun asDoubleStream(): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(seq.map { it.toDouble() }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun boxed(): Stream<Int> {
        state.markConsumed()
        return ReferenceStream(seq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun isParallel(): Boolean = state.parallel

    override fun sequential(): IntStream {
        state.parallel = false
        return this
    }

    override fun parallel(): IntStream {
        state.parallel = true
        return this
    }

    override fun unordered(): IntStream = this

    override fun onClose(closeHandler: Runnable): IntStream {
        if (state.consumed) throw IllegalStateException("stream has already been operated upon or closed")
        state.closeHandlers.add(closeHandler)
        return this
    }

    override fun close() = state.closeAll()

    override fun iterator(): PrimitiveIterator.OfInt {
        val iter = consumeSeq().iterator()
        return object : PrimitiveIterator.OfInt {
            override fun hasNext(): Boolean = iter.hasNext()
            override fun nextInt(): Int = iter.next()
            override fun next(): Int = nextInt()
            override fun forEachRemaining(action: IntConsumer) {
                while (hasNext()) action.accept(nextInt())
            }
            override fun remove() = throw UnsupportedOperationException("remove")
        }
    }

    override fun spliterator(): Spliterator.OfInt {
        val arr = toArray()
        return java.util.IntArraySpliterator(arr)
    }
}

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

internal class NativeLongStream(
    private val seq: Sequence<Long>,
    private val state: StreamState = StreamState()
) : LongStream {

    private fun consumeSeq(): Sequence<Long> {
        state.markConsumed()
        return seq
    }

    private fun derive(newSeq: Sequence<Long>): LongStream {
        state.markConsumed()
        return NativeLongStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun filter(predicate: LongPredicate): LongStream =
        derive(seq.filter { predicate.test(it) })

    override fun map(mapper: LongUnaryOperator): LongStream =
        derive(seq.map { mapper.applyAsLong(it) })

    override fun <U> mapToObj(mapper: LongFunction<out U>): Stream<U> {
        state.markConsumed()
        return ReferenceStream(seq.map { mapper.apply(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToInt(mapper: LongToIntFunction): IntStream {
        state.markConsumed()
        return NativeIntStream(seq.map { mapper.applyAsInt(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToDouble(mapper: LongToDoubleFunction): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(seq.map { mapper.applyAsDouble(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun flatMap(mapper: LongFunction<out LongStream?>): LongStream =
        derive(sequence {
            for (v in seq) {
                val sub = mapper.apply(v)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) yield(iter.nextLong())
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun distinct(): LongStream = derive(seq.distinct())

    override fun sorted(): LongStream = derive(sequence {
        val list = seq.toMutableList()
        list.sort()
        yieldAll(list)
    })

    override fun peek(action: LongConsumer): LongStream =
        derive(seq.onEach { action.accept(it) })

    override fun limit(maxSize: Long): LongStream {
        if (maxSize < 0) throw IllegalArgumentException("maxSize < 0: $maxSize")
        return derive(sequence {
            var remaining = maxSize
            val iter = seq.iterator()
            while (remaining > 0L && iter.hasNext()) {
                yield(iter.next())
                remaining--
            }
        })
    }

    override fun skip(n: Long): LongStream {
        if (n < 0) throw IllegalArgumentException("n < 0: $n")
        return derive(sequence {
            var toSkip = n
            val iter = seq.iterator()
            while (toSkip > 0L && iter.hasNext()) {
                iter.next()
                toSkip--
            }
            while (iter.hasNext()) yield(iter.next())
        })
    }

    override fun forEach(action: LongConsumer) {
        for (v in consumeSeq()) action.accept(v)
    }

    override fun forEachOrdered(action: LongConsumer) = forEach(action)

    override fun toArray(): LongArray = consumeSeq().toList().toLongArray()

    override fun reduce(identity: Long, op: LongBinaryOperator): Long {
        var res = identity
        for (v in consumeSeq()) res = op.applyAsLong(res, v)
        return res
    }

    override fun reduce(op: LongBinaryOperator): OptionalLong {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalLong.empty()
        var res = iter.next()
        while (iter.hasNext()) res = op.applyAsLong(res, iter.next())
        return OptionalLong.of(res)
    }

    override fun <R> collect(supplier: Supplier<R>, accumulator: ObjLongConsumer<R>, combiner: BiConsumer<R, R>): R {
        val c = supplier.get()
        for (v in consumeSeq()) accumulator.accept(c, v)
        return c
    }

    override fun sum(): Long {
        var s = 0L
        for (v in consumeSeq()) s += v
        return s
    }

    override fun min(): OptionalLong {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalLong.empty()
        var m = iter.next()
        while (iter.hasNext()) m = minOf(m, iter.next())
        return OptionalLong.of(m)
    }

    override fun max(): OptionalLong {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalLong.empty()
        var m = iter.next()
        while (iter.hasNext()) m = maxOf(m, iter.next())
        return OptionalLong.of(m)
    }

    override fun count(): Long {
        var c = 0L
        for (_v in consumeSeq()) c++
        return c
    }

    override fun average(): OptionalDouble {
        var c = 0L
        var sum = 0L
        for (v in consumeSeq()) {
            c++
            sum += v
        }
        return if (c == 0L) OptionalDouble.empty() else OptionalDouble.of(sum.toDouble() / c)
    }

    override fun summaryStatistics(): LongSummaryStatistics {
        val stats = LongSummaryStatistics()
        for (v in consumeSeq()) stats.accept(v)
        return stats
    }

    override fun anyMatch(predicate: LongPredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return true
        return false
    }

    override fun allMatch(predicate: LongPredicate): Boolean {
        for (v in consumeSeq()) if (!predicate.test(v)) return false
        return true
    }

    override fun noneMatch(predicate: LongPredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return false
        return true
    }

    override fun findFirst(): OptionalLong {
        val iter = consumeSeq().iterator()
        return if (iter.hasNext()) OptionalLong.of(iter.next()) else OptionalLong.empty()
    }

    override fun findAny(): OptionalLong = findFirst()

    override fun asDoubleStream(): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(seq.map { it.toDouble() }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun boxed(): Stream<Long> {
        state.markConsumed()
        return ReferenceStream(seq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun isParallel(): Boolean = state.parallel

    override fun sequential(): LongStream {
        state.parallel = false
        return this
    }

    override fun parallel(): LongStream {
        state.parallel = true
        return this
    }

    override fun unordered(): LongStream = this

    override fun onClose(closeHandler: Runnable): LongStream {
        if (state.consumed) throw IllegalStateException("stream has already been operated upon or closed")
        state.closeHandlers.add(closeHandler)
        return this
    }

    override fun close() = state.closeAll()

    override fun iterator(): PrimitiveIterator.OfLong {
        val iter = consumeSeq().iterator()
        return object : PrimitiveIterator.OfLong {
            override fun hasNext(): Boolean = iter.hasNext()
            override fun nextLong(): Long = iter.next()
            override fun next(): Long = nextLong()
            override fun forEachRemaining(action: LongConsumer) {
                while (hasNext()) action.accept(nextLong())
            }
            override fun remove() = throw UnsupportedOperationException("remove")
        }
    }

    override fun spliterator(): Spliterator.OfLong {
        val arr = toArray()
        return java.util.LongArraySpliterator(arr)
    }
}

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

internal class NativeDoubleStream(
    private val seq: Sequence<Double>,
    private val state: StreamState = StreamState()
) : DoubleStream {

    private fun consumeSeq(): Sequence<Double> {
        state.markConsumed()
        return seq
    }

    private fun derive(newSeq: Sequence<Double>): DoubleStream {
        state.markConsumed()
        return NativeDoubleStream(newSeq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun filter(predicate: DoublePredicate): DoubleStream =
        derive(seq.filter { predicate.test(it) })

    override fun map(mapper: DoubleUnaryOperator): DoubleStream =
        derive(seq.map { mapper.applyAsDouble(it) })

    override fun <U> mapToObj(mapper: DoubleFunction<out U>): Stream<U> {
        state.markConsumed()
        return ReferenceStream(seq.map { mapper.apply(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToInt(mapper: DoubleToIntFunction): IntStream {
        state.markConsumed()
        return NativeIntStream(seq.map { mapper.applyAsInt(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun mapToLong(mapper: DoubleToLongFunction): LongStream {
        state.markConsumed()
        return NativeLongStream(seq.map { mapper.applyAsLong(it) }, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun flatMap(mapper: DoubleFunction<out DoubleStream?>): DoubleStream =
        derive(sequence {
            for (v in seq) {
                val sub = mapper.apply(v)
                if (sub != null) {
                    try {
                        val iter = sub.iterator()
                        while (iter.hasNext()) yield(iter.nextDouble())
                    } finally {
                        sub.close()
                    }
                }
            }
        })

    override fun distinct(): DoubleStream = derive(seq.distinct())

    override fun sorted(): DoubleStream = derive(sequence {
        val list = seq.toMutableList()
        list.sort()
        yieldAll(list)
    })

    override fun peek(action: DoubleConsumer): DoubleStream =
        derive(seq.onEach { action.accept(it) })

    override fun limit(maxSize: Long): DoubleStream {
        if (maxSize < 0) throw IllegalArgumentException("maxSize < 0: $maxSize")
        return derive(sequence {
            var remaining = maxSize
            val iter = seq.iterator()
            while (remaining > 0L && iter.hasNext()) {
                yield(iter.next())
                remaining--
            }
        })
    }

    override fun skip(n: Long): DoubleStream {
        if (n < 0) throw IllegalArgumentException("n < 0: $n")
        return derive(sequence {
            var toSkip = n
            val iter = seq.iterator()
            while (toSkip > 0L && iter.hasNext()) {
                iter.next()
                toSkip--
            }
            while (iter.hasNext()) yield(iter.next())
        })
    }

    override fun forEach(action: DoubleConsumer) {
        for (v in consumeSeq()) action.accept(v)
    }

    override fun forEachOrdered(action: DoubleConsumer) = forEach(action)

    override fun toArray(): DoubleArray = consumeSeq().toList().toDoubleArray()

    override fun reduce(identity: Double, op: DoubleBinaryOperator): Double {
        var res = identity
        for (v in consumeSeq()) res = op.applyAsDouble(res, v)
        return res
    }

    override fun reduce(op: DoubleBinaryOperator): OptionalDouble {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalDouble.empty()
        var res = iter.next()
        while (iter.hasNext()) res = op.applyAsDouble(res, iter.next())
        return OptionalDouble.of(res)
    }

    override fun <R> collect(supplier: Supplier<R>, accumulator: ObjDoubleConsumer<R>, combiner: BiConsumer<R, R>): R {
        val c = supplier.get()
        for (v in consumeSeq()) accumulator.accept(c, v)
        return c
    }

    override fun sum(): Double {
        var s = 0.0
        for (v in consumeSeq()) s += v
        return s
    }

    override fun min(): OptionalDouble {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalDouble.empty()
        var m = iter.next()
        while (iter.hasNext()) m = minOf(m, iter.next())
        return OptionalDouble.of(m)
    }

    override fun max(): OptionalDouble {
        val iter = consumeSeq().iterator()
        if (!iter.hasNext()) return OptionalDouble.empty()
        var m = iter.next()
        while (iter.hasNext()) m = maxOf(m, iter.next())
        return OptionalDouble.of(m)
    }

    override fun count(): Long {
        var c = 0L
        for (_v in consumeSeq()) c++
        return c
    }

    override fun average(): OptionalDouble {
        var c = 0L
        var sum = 0.0
        for (v in consumeSeq()) {
            c++
            sum += v
        }
        return if (c == 0L) OptionalDouble.empty() else OptionalDouble.of(sum / c)
    }

    override fun summaryStatistics(): DoubleSummaryStatistics {
        val stats = DoubleSummaryStatistics()
        for (v in consumeSeq()) stats.accept(v)
        return stats
    }

    override fun anyMatch(predicate: DoublePredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return true
        return false
    }

    override fun allMatch(predicate: DoublePredicate): Boolean {
        for (v in consumeSeq()) if (!predicate.test(v)) return false
        return true
    }

    override fun noneMatch(predicate: DoublePredicate): Boolean {
        for (v in consumeSeq()) if (predicate.test(v)) return false
        return true
    }

    override fun findFirst(): OptionalDouble {
        val iter = consumeSeq().iterator()
        return if (iter.hasNext()) OptionalDouble.of(iter.next()) else OptionalDouble.empty()
    }

    override fun findAny(): OptionalDouble = findFirst()

    override fun boxed(): Stream<Double> {
        state.markConsumed()
        return ReferenceStream(seq, StreamState(state.parallel, state.closeHandlers, false))
    }

    override fun isParallel(): Boolean = state.parallel

    override fun sequential(): DoubleStream {
        state.parallel = false
        return this
    }

    override fun parallel(): DoubleStream {
        state.parallel = true
        return this
    }

    override fun unordered(): DoubleStream = this

    override fun onClose(closeHandler: Runnable): DoubleStream {
        if (state.consumed) throw IllegalStateException("stream has already been operated upon or closed")
        state.closeHandlers.add(closeHandler)
        return this
    }

    override fun close() = state.closeAll()

    override fun iterator(): PrimitiveIterator.OfDouble {
        val iter = consumeSeq().iterator()
        return object : PrimitiveIterator.OfDouble {
            override fun hasNext(): Boolean = iter.hasNext()
            override fun nextDouble(): Double = iter.next()
            override fun next(): Double = nextDouble()
            override fun forEachRemaining(action: DoubleConsumer) {
                while (hasNext()) action.accept(nextDouble())
            }
            override fun remove() = throw UnsupportedOperationException("remove")
        }
    }

    override fun spliterator(): Spliterator.OfDouble {
        val arr = toArray()
        return java.util.DoubleArraySpliterator(arr)
    }
}

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

private class CollectorImpl<T, A, R>(
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

actual class Collectors private constructor() {
    actual companion object {
        private val CH_ID = setOf(Collector.Characteristics.IDENTITY_FINISH)
        private val CH_UNORDERED_ID = setOf(Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH)
        private val CH_NOID = emptySet<Collector.Characteristics>()

        actual fun <T, C : MutableCollection<T>> toCollection(collectionFactory: Supplier<C>): Collector<T, *, C> {
            return CollectorImpl(
                collectionFactory,
                BiConsumer { c, t -> c.add(t) },
                BinaryOperator { r1, r2 -> r1.addAll(r2); r1 },
                Function { it },
                CH_ID
            )
        }

        actual fun <T> toList(): Collector<T, *, MutableList<T>> {
            return CollectorImpl(
                Supplier { ArrayList<T>() },
                BiConsumer { list, t -> list.add(t) },
                BinaryOperator { left, right -> left.addAll(right); left },
                Function { it },
                CH_ID
            )
        }

        actual fun <T> toSet(): Collector<T, *, MutableSet<T>> {
            return CollectorImpl(
                Supplier { HashSet<T>() },
                BiConsumer { set, t -> set.add(t) },
                BinaryOperator { left, right -> left.addAll(right); left },
                Function { it },
                CH_UNORDERED_ID
            )
        }

        actual fun joining(): Collector<CharSequence, *, String> {
            return CollectorImpl(
                Supplier { StringBuilder() },
                BiConsumer { sb, cs -> sb.append(cs) },
                BinaryOperator { r1, r2 -> r1.append(r2); r1 },
                Function { it.toString() },
                CH_NOID
            )
        }

        actual fun joining(delimiter: CharSequence): Collector<CharSequence, *, String> {
            return joining(delimiter, "", "")
        }

        actual fun joining(
            delimiter: CharSequence,
            prefix: CharSequence,
            suffix: CharSequence
        ): Collector<CharSequence, *, String> {
            return CollectorImpl(
                Supplier { StringJoiner(delimiter, prefix, suffix) },
                BiConsumer { sj, cs -> sj.add(cs) },
                BinaryOperator { r1, r2 -> r1.merge(r2) },
                Function { it.toString() },
                CH_NOID
            )
        }

        actual fun <T, U, A, R> mapping(
            mapper: Function<in T, out U>,
            downstream: Collector<in U, A, R>
        ): Collector<T, *, R> {
            val downAcc = downstream.accumulator()
            return CollectorImpl(
                downstream.supplier(),
                BiConsumer { r, t -> downAcc.accept(r, mapper.apply(t)) },
                downstream.combiner(),
                downstream.finisher(),
                downstream.characteristics()
            )
        }

        actual fun <T, A, R, RR> collectingAndThen(
            downstream: Collector<T, A, R>,
            finisher: Function<R, RR>
        ): Collector<T, A, RR> {
            val chars = downstream.characteristics().minus(Collector.Characteristics.IDENTITY_FINISH)
            val downFinisher = downstream.finisher()
            return CollectorImpl(
                downstream.supplier(),
                downstream.accumulator(),
                downstream.combiner(),
                Function { a ->
                    @Suppress("UNCHECKED_CAST")
                    val r = if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        a as R
                    } else {
                        downFinisher.apply(a)
                    }
                    finisher.apply(r)
                },
                chars
            )
        }

        actual fun <T> counting(): Collector<T, *, Long> {
            return reducing(0L, Function { 1L }, BinaryOperator { a, b -> a + b })
        }

        actual fun <T> minBy(comparator: Comparator<in T>): Collector<T, *, Optional<T>> {
            return reducing(BinaryOperator { a, b -> if (comparator.compare(a, b) <= 0) a else b })
        }

        actual fun <T> maxBy(comparator: Comparator<in T>): Collector<T, *, Optional<T>> {
            return reducing(BinaryOperator { a, b -> if (comparator.compare(a, b) >= 0) a else b })
        }

        actual fun <T> summingInt(mapper: ToIntFunction<in T>): Collector<T, *, Int> {
            return CollectorImpl(
                Supplier { IntArray(1) },
                BiConsumer { a, t -> a[0] += mapper.applyAsInt(t) },
                BinaryOperator { a, b -> a[0] += b[0]; a },
                Function { it[0] },
                CH_NOID
            )
        }

        actual fun <T> summingLong(mapper: ToLongFunction<in T>): Collector<T, *, Long> {
            return CollectorImpl(
                Supplier { LongArray(1) },
                BiConsumer { a, t -> a[0] += mapper.applyAsLong(t) },
                BinaryOperator { a, b -> a[0] += b[0]; a },
                Function { it[0] },
                CH_NOID
            )
        }

        actual fun <T> summingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, Double> {
            return CollectorImpl(
                Supplier { DoubleArray(1) },
                BiConsumer { a, t -> a[0] += mapper.applyAsDouble(t) },
                BinaryOperator { a, b -> a[0] += b[0]; a },
                Function { it[0] },
                CH_NOID
            )
        }

        actual fun <T> averagingInt(mapper: ToIntFunction<in T>): Collector<T, *, Double> {
            return CollectorImpl(
                Supplier { LongArray(2) },
                BiConsumer { a, t -> a[0] += mapper.applyAsInt(t).toLong(); a[1]++ },
                BinaryOperator { a, b -> a[0] += b[0]; a[1] += b[1]; a },
                Function { a -> if (a[1] == 0L) 0.0 else a[0].toDouble() / a[1] },
                CH_NOID
            )
        }

        actual fun <T> averagingLong(mapper: ToLongFunction<in T>): Collector<T, *, Double> {
            return CollectorImpl(
                Supplier { LongArray(2) },
                BiConsumer { a, t -> a[0] += mapper.applyAsLong(t); a[1]++ },
                BinaryOperator { a, b -> a[0] += b[0]; a[1] += b[1]; a },
                Function { a -> if (a[1] == 0L) 0.0 else a[0].toDouble() / a[1] },
                CH_NOID
            )
        }

        actual fun <T> averagingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, Double> {
            return CollectorImpl(
                Supplier { DoubleArray(2) },
                BiConsumer { a, t -> a[0] += mapper.applyAsDouble(t); a[1] += 1.0 },
                BinaryOperator { a, b -> a[0] += b[0]; a[1] += b[1]; a },
                Function { a -> if (a[1] == 0.0) 0.0 else a[0] / a[1] },
                CH_NOID
            )
        }

        actual fun <T> reducing(identity: T, op: BinaryOperator<T>): Collector<T, *, T> {
            class Box(var value: T)
            return CollectorImpl(
                Supplier { Box(identity) },
                BiConsumer { b, t -> b.value = op.apply(b.value, t) },
                BinaryOperator { a, b -> a.value = op.apply(a.value, b.value); a },
                Function { it.value },
                CH_NOID
            )
        }

        actual fun <T> reducing(op: BinaryOperator<T>): Collector<T, *, Optional<T>> {
            class OptionalBox(var present: Boolean = false, var value: T? = null)
            return CollectorImpl(
                Supplier { OptionalBox() },
                BiConsumer { b, t ->
                    if (b.present) {
                        @Suppress("UNCHECKED_CAST")
                        b.value = op.apply(b.value as T, t)
                    } else {
                        b.value = t
                        b.present = true
                    }
                },
                BinaryOperator { a, b ->
                    if (!a.present) b
                    else if (!b.present) a
                    else {
                        @Suppress("UNCHECKED_CAST")
                        a.value = op.apply(a.value as T, b.value as T)
                        a
                    }
                },
                Function { b -> if (b.present) Optional.ofNullable(b.value) else Optional.empty() },
                CH_NOID
            )
        }

        actual fun <T, U> reducing(
            identity: U,
            mapper: Function<in T, out U>,
            op: BinaryOperator<U>
        ): Collector<T, *, U> {
            class Box(var value: U)
            return CollectorImpl(
                Supplier { Box(identity) },
                BiConsumer { b, t -> b.value = op.apply(b.value, mapper.apply(t)) },
                BinaryOperator { a, b -> a.value = op.apply(a.value, b.value); a },
                Function { it.value },
                CH_NOID
            )
        }

        actual fun <T, K> groupingBy(classifier: Function<in T, out K>): Collector<T, *, MutableMap<K, MutableList<T>>> {
            return groupingBy(classifier, toList())
        }

        actual fun <T, K, A, D> groupingBy(
            classifier: Function<in T, out K>,
            downstream: Collector<in T, A, D>
        ): Collector<T, *, MutableMap<K, D>> {
            return groupingBy(classifier, Supplier { HashMap<K, D>() }, downstream)
        }

        actual fun <T, K, D, A, M : MutableMap<K, D>> groupingBy(
            classifier: Function<in T, out K>,
            mapFactory: Supplier<M>,
            downstream: Collector<in T, A, D>
        ): Collector<T, *, M> {
            val downSupplier = downstream.supplier()
            val downAccumulator = downstream.accumulator()
            val downCombiner = downstream.combiner()
            val downFinisher = downstream.finisher()
            val isIdentity = downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)

            return CollectorImpl<T, MutableMap<K, A>, M>(
                Supplier { LinkedHashMap<K, A>() },
                BiConsumer { m, t ->
                    val key = classifier.apply(t) ?: throw NullPointerException("element cannot be mapped to a null key")
                    val container = m.getOrPut(key) { downSupplier.get() }
                    downAccumulator.accept(container, t)
                },
                BinaryOperator { m1, m2 ->
                    for ((k, v) in m2) {
                        val existing = m1[k]
                        m1[k] = if (existing == null) v else downCombiner.apply(existing, v)
                    }
                    m1
                },
                Function { intermediate ->
                    val res = mapFactory.get()
                    for ((k, a) in intermediate) {
                        @Suppress("UNCHECKED_CAST")
                        res[k] = if (isIdentity) a as D else downFinisher.apply(a)
                    }
                    res
                },
                CH_NOID
            )
        }

        actual fun <T> partitioningBy(predicate: Predicate<in T>): Collector<T, *, MutableMap<Boolean, MutableList<T>>> {
            return partitioningBy(predicate, toList())
        }

        actual fun <T, D, A> partitioningBy(
            predicate: Predicate<in T>,
            downstream: Collector<in T, A, D>
        ): Collector<T, *, MutableMap<Boolean, D>> {
            val downSupplier = downstream.supplier()
            val downAcc = downstream.accumulator()
            val downComb = downstream.combiner()
            val downFin = downstream.finisher()
            val isIdentity = downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)
            class Part<A>(var forTrue: A, var forFalse: A)

            return CollectorImpl<T, Part<A>, MutableMap<Boolean, D>>(
                Supplier { Part(downSupplier.get(), downSupplier.get()) },
                BiConsumer { p, t ->
                    if (predicate.test(t)) downAcc.accept(p.forTrue, t) else downAcc.accept(p.forFalse, t)
                },
                BinaryOperator { left, right ->
                    Part(downComb.apply(left.forTrue, right.forTrue), downComb.apply(left.forFalse, right.forFalse))
                },
                Function { p ->
                    val map = HashMap<Boolean, D>()
                    @Suppress("UNCHECKED_CAST")
                    map[true] = if (isIdentity) p.forTrue as D else downFin.apply(p.forTrue)
                    @Suppress("UNCHECKED_CAST")
                    map[false] = if (isIdentity) p.forFalse as D else downFin.apply(p.forFalse)
                    map
                },
                CH_NOID
            )
        }

        actual fun <T, K, U> toMap(
            keyMapper: Function<in T, out K>,
            valueMapper: Function<in T, out U>
        ): Collector<T, *, MutableMap<K, U>> {
            return toMap(
                keyMapper,
                valueMapper,
                BinaryOperator { u, _ -> throw IllegalStateException("Duplicate key (attempted merging values $u)") },
                Supplier { HashMap<K, U>() }
            )
        }

        actual fun <T, K, U> toMap(
            keyMapper: Function<in T, out K>,
            valueMapper: Function<in T, out U>,
            mergeFunction: BinaryOperator<U>
        ): Collector<T, *, MutableMap<K, U>> {
            return toMap(keyMapper, valueMapper, mergeFunction, Supplier { HashMap<K, U>() })
        }

        actual fun <T, K, U, M : MutableMap<K, U>> toMap(
            keyMapper: Function<in T, out K>,
            valueMapper: Function<in T, out U>,
            mergeFunction: BinaryOperator<U>,
            mapSupplier: Supplier<M>
        ): Collector<T, *, M> {
            return CollectorImpl(
                mapSupplier,
                BiConsumer { map, element ->
                    val k = keyMapper.apply(element)
                    val v = valueMapper.apply(element) ?: throw NullPointerException()
                    val old = map[k]
                    if (old == null && !map.containsKey(k)) {
                        map[k] = v
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val merged = mergeFunction.apply(old as U, v)
                        if (merged == null) map.remove(k) else map[k] = merged
                    }
                },
                BinaryOperator { m1, m2 ->
                    for ((k, v) in m2) {
                        val old = m1[k]
                        if (old == null && !m1.containsKey(k)) {
                            m1[k] = v
                        } else {
                            @Suppress("UNCHECKED_CAST")
                            val merged = mergeFunction.apply(old as U, v)
                            if (merged == null) m1.remove(k) else m1[k] = merged
                        }
                    }
                    m1
                },
                Function { it },
                CH_ID
            )
        }

        actual fun <T> summarizingInt(mapper: ToIntFunction<in T>): Collector<T, *, IntSummaryStatistics> {
            return CollectorImpl(
                Supplier { IntSummaryStatistics() },
                BiConsumer { r, t -> r.accept(mapper.applyAsInt(t)) },
                BinaryOperator { l, r -> l.combine(r); l },
                Function { it },
                CH_ID
            )
        }

        actual fun <T> summarizingLong(mapper: ToLongFunction<in T>): Collector<T, *, LongSummaryStatistics> {
            return CollectorImpl(
                Supplier { LongSummaryStatistics() },
                BiConsumer { r, t -> r.accept(mapper.applyAsLong(t)) },
                BinaryOperator { l, r -> l.combine(r); l },
                Function { it },
                CH_ID
            )
        }

        actual fun <T> summarizingDouble(mapper: ToDoubleFunction<in T>): Collector<T, *, DoubleSummaryStatistics> {
            return CollectorImpl(
                Supplier { DoubleSummaryStatistics() },
                BiConsumer { r, t -> r.accept(mapper.applyAsDouble(t)) },
                BinaryOperator { l, r -> l.combine(r); l },
                Function { it },
                CH_ID
            )
        }
    }
}

actual class StreamSupport private constructor() {
    actual companion object {
        actual fun <T> stream(spliterator: Spliterator<T>, parallel: Boolean): Stream<T> {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.next())
            }
            return ReferenceStream(seq, StreamState(parallel = parallel))
        }

        actual fun <T> stream(
            supplier: Supplier<out Spliterator<T>>,
            characteristics: Int,
            parallel: Boolean
        ): Stream<T> {
            val seq = sequence {
                val iter = Spliterators.iterator(supplier.get())
                while (iter.hasNext()) yield(iter.next())
            }
            return ReferenceStream(seq, StreamState(parallel = parallel))
        }

        actual fun intStream(spliterator: Spliterator.OfInt, parallel: Boolean): IntStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextInt())
            }
            return NativeIntStream(seq, StreamState(parallel = parallel))
        }

        actual fun longStream(spliterator: Spliterator.OfLong, parallel: Boolean): LongStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextLong())
            }
            return NativeLongStream(seq, StreamState(parallel = parallel))
        }

        actual fun doubleStream(spliterator: Spliterator.OfDouble, parallel: Boolean): DoubleStream {
            val seq = sequence {
                val iter = Spliterators.iterator(spliterator)
                while (iter.hasNext()) yield(iter.nextDouble())
            }
            return NativeDoubleStream(seq, StreamState(parallel = parallel))
        }
    }
}
