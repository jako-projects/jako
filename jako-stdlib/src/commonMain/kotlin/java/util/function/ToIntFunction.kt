@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ToIntFunction<T> {
    fun applyAsInt(value: T): Int
}
