@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class OptionalLong {
    fun getAsLong(): Long
    fun isPresent(): Boolean
    fun ifPresent(consumer: (Long) -> Unit)
    fun orElse(other: Long): Long
    fun orElseGet(other: () -> Long): Long
    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Long

    companion object {
        fun empty(): OptionalLong
        fun of(value: Long): OptionalLong
    }
}
