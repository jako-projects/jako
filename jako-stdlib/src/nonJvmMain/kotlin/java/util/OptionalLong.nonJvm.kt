package java.util

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
