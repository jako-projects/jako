@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface BiPredicate<T, U> {
    fun test(t: T, u: U): Boolean
}
