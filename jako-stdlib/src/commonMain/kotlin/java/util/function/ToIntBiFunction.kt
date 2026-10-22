@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ToIntBiFunction<T, U> {
    fun applyAsInt(t: T, u: U): Int
}
