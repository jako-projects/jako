@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class Optional<T> {
    fun get(): T
    fun isPresent(): Boolean
    fun ifPresent(consumer: (T) -> Unit)
    fun filter(predicate: (T) -> Boolean): Optional<T>
    fun <U> map(mapper: (T) -> U?): Optional<U>
    fun <U> flatMap(mapper: (T) -> Optional<U>): Optional<U>
    fun orElse(other: T): T
    fun orElseGet(other: () -> T): T
    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): T

    companion object {
        fun <T> empty(): Optional<T>
        fun <T : Any> of(value: T): Optional<T>
        fun <T> ofNullable(value: T?): Optional<T>
    }
}

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
