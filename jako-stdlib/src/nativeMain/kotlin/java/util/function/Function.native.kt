package java.util.function

actual fun interface Function<T, R> {
    actual fun apply(t: T): R
}

actual fun interface BiFunction<T, U, R> {
    actual fun apply(t: T, u: U): R
}

actual fun interface Consumer<T> {
    actual fun accept(t: T)
}

actual fun interface BiConsumer<T, U> {
    actual fun accept(t: T, u: U)
}

actual fun interface Predicate<T> {
    actual fun test(t: T): Boolean
}

actual fun interface BiPredicate<T, U> {
    actual fun test(t: T, u: U): Boolean
}

actual fun interface Supplier<T> {
    actual fun get(): T
}

actual fun interface UnaryOperator<T> : Function<T, T>

actual fun interface BinaryOperator<T> : BiFunction<T, T, T>

actual fun interface BooleanSupplier {
    actual fun getAsBoolean(): Boolean
}

actual fun interface IntSupplier {
    actual fun getAsInt(): Int
}

actual fun interface LongSupplier {
    actual fun getAsLong(): Long
}

actual fun interface DoubleSupplier {
    actual fun getAsDouble(): Double
}

actual fun interface IntConsumer {
    actual fun accept(value: Int)
}

actual fun interface LongConsumer {
    actual fun accept(value: Long)
}

actual fun interface DoubleConsumer {
    actual fun accept(value: Double)
}

actual fun interface ObjIntConsumer<T> {
    actual fun accept(t: T, value: Int)
}

actual fun interface ObjLongConsumer<T> {
    actual fun accept(t: T, value: Long)
}

actual fun interface ObjDoubleConsumer<T> {
    actual fun accept(t: T, value: Double)
}

actual fun interface IntPredicate {
    actual fun test(value: Int): Boolean
}

actual fun interface LongPredicate {
    actual fun test(value: Long): Boolean
}

actual fun interface DoublePredicate {
    actual fun test(value: Double): Boolean
}

actual fun interface IntFunction<R> {
    actual fun apply(value: Int): R
}

actual fun interface LongFunction<R> {
    actual fun apply(value: Long): R
}

actual fun interface DoubleFunction<R> {
    actual fun apply(value: Double): R
}

actual fun interface ToIntFunction<T> {
    actual fun applyAsInt(value: T): Int
}

actual fun interface ToLongFunction<T> {
    actual fun applyAsLong(value: T): Long
}

actual fun interface ToDoubleFunction<T> {
    actual fun applyAsDouble(value: T): Double
}

actual fun interface ToIntBiFunction<T, U> {
    actual fun applyAsInt(t: T, u: U): Int
}

actual fun interface ToLongBiFunction<T, U> {
    actual fun applyAsLong(t: T, u: U): Long
}

actual fun interface ToDoubleBiFunction<T, U> {
    actual fun applyAsDouble(t: T, u: U): Double
}

actual fun interface IntUnaryOperator {
    actual fun applyAsInt(operand: Int): Int
}

actual fun interface LongUnaryOperator {
    actual fun applyAsLong(operand: Long): Long
}

actual fun interface DoubleUnaryOperator {
    actual fun applyAsDouble(operand: Double): Double
}

actual fun interface IntBinaryOperator {
    actual fun applyAsInt(left: Int, right: Int): Int
}

actual fun interface LongBinaryOperator {
    actual fun applyAsLong(left: Long, right: Long): Long
}

actual fun interface DoubleBinaryOperator {
    actual fun applyAsDouble(left: Double, right: Double): Double
}

actual fun interface IntToLongFunction {
    actual fun applyAsLong(value: Int): Long
}

actual fun interface IntToDoubleFunction {
    actual fun applyAsDouble(value: Int): Double
}

actual fun interface LongToIntFunction {
    actual fun applyAsInt(value: Long): Int
}

actual fun interface LongToDoubleFunction {
    actual fun applyAsDouble(value: Long): Double
}

actual fun interface DoubleToIntFunction {
    actual fun applyAsInt(value: Double): Int
}

actual fun interface DoubleToLongFunction {
    actual fun applyAsLong(value: Double): Long
}

