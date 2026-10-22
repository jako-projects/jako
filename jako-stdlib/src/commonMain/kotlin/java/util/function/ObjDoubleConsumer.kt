@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ObjDoubleConsumer<T> {
    fun accept(t: T, value: Double)
}
