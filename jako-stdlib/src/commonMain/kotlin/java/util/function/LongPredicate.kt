@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface LongPredicate {
    fun test(value: Long): Boolean
}
