package java.util.concurrent.atomic

import java.io.Serializable

actual open class LongAdder actual constructor() : Number(), Serializable {
    private val base = kotlin.concurrent.AtomicLong(0L)

    actual open fun add(x: Long) {
        base.addAndGet(x)
    }

    actual open fun increment() {
        base.addAndGet(1L)
    }

    actual open fun decrement() {
        base.addAndGet(-1L)
    }

    actual open fun sum(): Long = base.value

    actual open fun reset() {
        base.value = 0L
    }

    actual open fun sumThenReset(): Long {
        while (true) {
            val current = base.value
            if (base.compareAndSet(current, 0L)) {
                return current
            }
        }
    }

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toByte()
    actual override fun toShort(): Short = sum().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum().toDouble()
}
