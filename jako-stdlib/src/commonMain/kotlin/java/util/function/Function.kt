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
