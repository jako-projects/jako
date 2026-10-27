package java.util

actual class Optional<T> private constructor(private val value: T?) {

    actual fun get(): T {
        return value ?: throw NoSuchElementException("No value present")
    }

    actual fun isPresent(): Boolean = value != null

    actual fun ifPresent(consumer: (T) -> Unit) {
        if (value != null) {
            consumer(value)
        }
    }

    actual fun filter(predicate: (T) -> Boolean): Optional<T> {
        val current = value
        if (current == null) {
            return this
        } else {
            return if (predicate(current)) this else empty()
        }
    }

    actual fun <U> map(mapper: (T) -> U?): Optional<U> {
        val current = value
        if (current == null) {
            return empty()
        } else {
            return ofNullable(mapper(current))
        }
    }

    actual fun <U> flatMap(mapper: (T) -> Optional<U>): Optional<U> {
        val current = value
        if (current == null) {
            return empty()
        } else {
            return mapper(current)
        }
    }

    actual fun orElse(other: T): T = value ?: other

    actual fun orElseGet(other: () -> T): T = value ?: other()

    actual fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): T {
        return value ?: throw exceptionSupplier()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Optional<*>) return false
        return this.value == other.value
    }

    override fun hashCode(): Int = value?.hashCode() ?: 0

    override fun toString(): String = if (value != null) "Optional[$value]" else "Optional.empty"

    actual companion object {
        private val EMPTY: Optional<Any?> = Optional(null)

        @Suppress("UNCHECKED_CAST")
        actual fun <T> empty(): Optional<T> = EMPTY as Optional<T>

        actual fun <T : Any> of(value: T): Optional<T> = Optional(value)

        actual fun <T> ofNullable(value: T?): Optional<T> {
            return if (value == null) empty() else Optional(value)
        }
    }
}
