@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface ToDoubleBiFunction<T, U> {
    fun applyAsDouble(t: T, u: U): Double
}
