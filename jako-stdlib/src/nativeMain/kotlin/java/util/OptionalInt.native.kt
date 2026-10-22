package java.util

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
