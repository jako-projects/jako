@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface Consumer<T> {
    fun accept(t: T)
}
