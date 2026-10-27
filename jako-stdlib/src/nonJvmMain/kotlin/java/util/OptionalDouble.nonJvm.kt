package java.util

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
