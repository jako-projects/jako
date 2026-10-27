package java.util.stream

import java.util.DoubleSummaryStatistics
import java.util.IntSummaryStatistics
import java.util.LongSummaryStatistics
import java.util.Optional
import java.util.StringJoiner
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToDoubleFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongFunction

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
