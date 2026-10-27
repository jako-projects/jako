package java.util.concurrent.atomic

import java.io.Serializable

actual open class DoubleAdder actual constructor() : Number(), Serializable {
    private val baseBits = AtomicLong(0.0.toRawBits())

    actual open fun add(x: Double) {
        while (true) {
            val currentBits = baseBits.get()
            val nextVal = Double.fromBits(currentBits) + x
            if (baseBits.compareAndSet(currentBits, nextVal.toRawBits())) {
                return
            }
        }
    }

    actual open fun sum(): Double = Double.fromBits(baseBits.get())

    actual open fun reset() {
        baseBits.set(0.0.toRawBits())
    }

    actual open fun sumThenReset(): Double {
        return Double.fromBits(baseBits.getAndSet(0.0.toRawBits()))
    }

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toInt().toByte()
    actual override fun toShort(): Short = sum().toInt().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum().toLong()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum()
}
