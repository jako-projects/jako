@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface BinaryOperator<T> : BiFunction<T, T, T>
