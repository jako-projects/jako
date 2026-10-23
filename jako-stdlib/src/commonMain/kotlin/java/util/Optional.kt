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
