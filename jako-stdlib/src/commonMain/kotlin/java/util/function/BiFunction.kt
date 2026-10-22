@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface BiFunction<T, U, R> {
    fun apply(t: T, u: U): R
}
