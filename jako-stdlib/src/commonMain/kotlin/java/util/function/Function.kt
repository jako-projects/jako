@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface Function<T, R> {
    fun apply(t: T): R
}
