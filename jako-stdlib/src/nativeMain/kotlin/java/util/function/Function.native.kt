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
