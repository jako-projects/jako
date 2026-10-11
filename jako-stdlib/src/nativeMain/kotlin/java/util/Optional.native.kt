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

actual class OptionalInt private constructor(private val isPresent: Boolean, private val value: Int) {

    actual fun getAsInt(): Int {
        if (!isPresent) throw NoSuchElementException("No value present")
        return value
    }

    actual fun isPresent(): Boolean = isPresent

    actual fun ifPresent(consumer: (Int) -> Unit) {
        if (isPresent) {
            consumer(value)
        }
    }

    actual fun orElse(other: Int): Int = if (isPresent) value else other

    actual fun orElseGet(other: () -> Int): Int = if (isPresent) value else other()

    actual fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Int {
        if (!isPresent) throw exceptionSupplier()
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OptionalInt) return false
        return this.isPresent == other.isPresent && this.value == other.value
    }

    override fun hashCode(): Int = if (isPresent) value.hashCode() else 0

    override fun toString(): String = if (isPresent) "OptionalInt[$value]" else "OptionalInt.empty"

    actual companion object {
        private val EMPTY = OptionalInt(false, 0)

        actual fun empty(): OptionalInt = EMPTY

        actual fun of(value: Int): OptionalInt = OptionalInt(true, value)
    }
}

actual class OptionalLong private constructor(private val isPresent: Boolean, private val value: Long) {

    actual fun getAsLong(): Long {
        if (!isPresent) throw NoSuchElementException("No value present")
        return value
    }

    actual fun isPresent(): Boolean = isPresent

    actual fun ifPresent(consumer: (Long) -> Unit) {
        if (isPresent) {
            consumer(value)
        }
    }

    actual fun orElse(other: Long): Long = if (isPresent) value else other

    actual fun orElseGet(other: () -> Long): Long = if (isPresent) value else other()

    actual fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Long {
        if (!isPresent) throw exceptionSupplier()
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OptionalLong) return false
        return this.isPresent == other.isPresent && this.value == other.value
    }

    override fun hashCode(): Int = if (isPresent) (value xor (value ushr 32)).toInt() else 0

    override fun toString(): String = if (isPresent) "OptionalLong[$value]" else "OptionalLong.empty"

    actual companion object {
        private val EMPTY = OptionalLong(false, 0L)

        actual fun empty(): OptionalLong = EMPTY

        actual fun of(value: Long): OptionalLong = OptionalLong(true, value)
    }
}

actual class OptionalDouble private constructor(private val isPresent: Boolean, private val value: Double) {

    actual fun getAsDouble(): Double {
        if (!isPresent) throw NoSuchElementException("No value present")
        return value
    }

    actual fun isPresent(): Boolean = isPresent

    actual fun ifPresent(consumer: (Double) -> Unit) {
        if (isPresent) {
            consumer(value)
        }
    }

    actual fun orElse(other: Double): Double = if (isPresent) value else other

    actual fun orElseGet(other: () -> Double): Double = if (isPresent) value else other()

    actual fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): Double {
        if (!isPresent) throw exceptionSupplier()
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OptionalDouble) return false
        return this.isPresent == other.isPresent && this.value.compareTo(other.value) == 0
    }

    override fun hashCode(): Int = if (isPresent) value.toBits().hashCode() else 0

    override fun toString(): String = if (isPresent) "OptionalDouble[$value]" else "OptionalDouble.empty"

    actual companion object {
        private val EMPTY = OptionalDouble(false, 0.0)

        actual fun empty(): OptionalDouble = EMPTY

        actual fun of(value: Double): OptionalDouble = OptionalDouble(true, value)
    }
}
