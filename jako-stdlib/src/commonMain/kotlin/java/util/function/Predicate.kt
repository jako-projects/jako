@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface Predicate<T> {
    fun test(t: T): Boolean
}
