@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface Function<T, R> {
    fun apply(t: T): R
}

expect fun interface BiFunction<T, U, R> {
    fun apply(t: T, u: U): R
}

expect fun interface Consumer<T> {
    fun accept(t: T)
}

expect fun interface BiConsumer<T, U> {
    fun accept(t: T, u: U)
}

expect fun interface Predicate<T> {
    fun test(t: T): Boolean
}

expect fun interface BiPredicate<T, U> {
    fun test(t: T, u: U): Boolean
}

expect fun interface Supplier<T> {
    fun get(): T
}

expect fun interface UnaryOperator<T> : Function<T, T>

expect fun interface BinaryOperator<T> : BiFunction<T, T, T>

expect fun interface BooleanSupplier {
    fun getAsBoolean(): Boolean
}

expect fun interface IntSupplier {
    fun getAsInt(): Int
}

expect fun interface LongSupplier {
    fun getAsLong(): Long
}

expect fun interface DoubleSupplier {
    fun getAsDouble(): Double
}

expect fun interface IntConsumer {
    fun accept(value: Int)
}

expect fun interface LongConsumer {
    fun accept(value: Long)
}

expect fun interface DoubleConsumer {
    fun accept(value: Double)
}

expect fun interface ObjIntConsumer<T> {
    fun accept(t: T, value: Int)
}

expect fun interface ObjLongConsumer<T> {
    fun accept(t: T, value: Long)
}

expect fun interface ObjDoubleConsumer<T> {
    fun accept(t: T, value: Double)
}

expect fun interface IntPredicate {
    fun test(value: Int): Boolean
}

expect fun interface LongPredicate {
    fun test(value: Long): Boolean
}

expect fun interface DoublePredicate {
    fun test(value: Double): Boolean
}

expect fun interface IntFunction<R> {
    fun apply(value: Int): R
}

expect fun interface LongFunction<R> {
    fun apply(value: Long): R
}

expect fun interface DoubleFunction<R> {
    fun apply(value: Double): R
}

expect fun interface ToIntFunction<T> {
    fun applyAsInt(value: T): Int
}

expect fun interface ToLongFunction<T> {
    fun applyAsLong(value: T): Long
}

expect fun interface ToDoubleFunction<T> {
    fun applyAsDouble(value: T): Double
}

expect fun interface ToIntBiFunction<T, U> {
    fun applyAsInt(t: T, u: U): Int
}

expect fun interface ToLongBiFunction<T, U> {
    fun applyAsLong(t: T, u: U): Long
}

expect fun interface ToDoubleBiFunction<T, U> {
    fun applyAsDouble(t: T, u: U): Double
}

expect fun interface IntUnaryOperator {
    fun applyAsInt(operand: Int): Int
}

expect fun interface LongUnaryOperator {
    fun applyAsLong(operand: Long): Long
}

expect fun interface DoubleUnaryOperator {
    fun applyAsDouble(operand: Double): Double
}

expect fun interface IntBinaryOperator {
    fun applyAsInt(left: Int, right: Int): Int
}

expect fun interface LongBinaryOperator {
    fun applyAsLong(left: Long, right: Long): Long
}

expect fun interface DoubleBinaryOperator {
    fun applyAsDouble(left: Double, right: Double): Double
}

expect fun interface IntToLongFunction {
    fun applyAsLong(value: Int): Long
}

expect fun interface IntToDoubleFunction {
    fun applyAsDouble(value: Int): Double
}

expect fun interface LongToIntFunction {
    fun applyAsInt(value: Long): Int
}

expect fun interface LongToDoubleFunction {
    fun applyAsDouble(value: Long): Double
}

expect fun interface DoubleToIntFunction {
    fun applyAsInt(value: Double): Int
}

expect fun interface DoubleToLongFunction {
    fun applyAsLong(value: Double): Long
}

