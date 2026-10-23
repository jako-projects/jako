@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ToLongBiFunction<T, U> {
    fun applyAsLong(t: T, u: U): Long
}
