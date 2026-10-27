package java.util.concurrent.atomic

import java.io.Serializable

actual open class LongAdder actual constructor() : Number(), Serializable {
    private val base = AtomicLong(0L)

    actual open fun add(x: Long) {
        base.addAndGet(x)
    }

    actual open fun increment() {
        base.addAndGet(1L)
    }

    actual open fun decrement() {
        base.addAndGet(-1L)
    }

    actual open fun sum(): Long = base.get()

    actual open fun reset() {
        base.set(0L)
    }

    actual open fun sumThenReset(): Long = base.getAndSet(0L)

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toByte()
    actual override fun toShort(): Short = sum().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum().toDouble()
}
