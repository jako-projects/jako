@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface DoublePredicate {
    fun test(value: Double): Boolean
}
