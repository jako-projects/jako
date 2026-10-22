@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class OptionalInt {
    fun getAsInt(): Int
    fun isPresent(): Boolean
    fun ifPresent(consumer: (Int) -> Unit)
    fun orElse(other: Int): Int
    fun orElseGet(other: () -> Int): Int
    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Int

    companion object {
        fun empty(): OptionalInt
        fun of(value: Int): OptionalInt
    }
}
