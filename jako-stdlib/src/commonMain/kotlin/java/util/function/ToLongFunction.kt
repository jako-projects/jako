@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ToLongFunction<T> {
    fun applyAsLong(value: T): Long
}
