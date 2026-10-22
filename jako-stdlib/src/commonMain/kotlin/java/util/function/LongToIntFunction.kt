@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface LongToIntFunction {
    fun applyAsInt(value: Long): Int
}
