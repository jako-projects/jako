@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class OptionalDouble {
    fun getAsDouble(): Double
    fun isPresent(): Boolean
    fun ifPresent(consumer: (Double) -> Unit)
    fun orElse(other: Double): Double
    fun orElseGet(other: () -> Double): Double
    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Double

    companion object {
        fun empty(): OptionalDouble
        fun of(value: Double): OptionalDouble
    }
}
