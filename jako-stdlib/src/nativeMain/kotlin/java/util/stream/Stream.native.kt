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
import java.util.Spliterators
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.DoubleBinaryOperator
import java.util.function.DoubleConsumer
import java.util.function.DoubleFunction
import java.util.function.DoublePredicate
import java.util.function.DoubleToIntFunction
import java.util.function.DoubleToLongFunction
import java.util.function.DoubleUnaryOperator
import java.util.function.Function
import java.util.function.IntBinaryOperator
import java.util.function.IntConsumer
import java.util.function.IntFunction
import java.util.function.IntPredicate
import java.util.function.IntToDoubleFunction
import java.util.function.IntToLongFunction
import java.util.function.IntUnaryOperator
import java.util.function.LongBinaryOperator
import java.util.function.LongConsumer
import java.util.function.LongFunction
import java.util.function.LongPredicate
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
