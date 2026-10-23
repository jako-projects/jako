@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface BiConsumer<T, U> {
    fun accept(t: T, u: U)
}
