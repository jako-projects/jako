@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface LongConsumer {
    fun accept(value: Long)
}
