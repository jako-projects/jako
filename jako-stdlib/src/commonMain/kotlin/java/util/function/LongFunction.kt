@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface LongFunction<R> {
    fun apply(value: Long): R
}
