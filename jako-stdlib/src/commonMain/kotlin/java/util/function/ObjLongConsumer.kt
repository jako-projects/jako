@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ObjLongConsumer<T> {
    fun accept(t: T, value: Long)
}
