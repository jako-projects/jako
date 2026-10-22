@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ObjIntConsumer<T> {
    fun accept(t: T, value: Int)
}
