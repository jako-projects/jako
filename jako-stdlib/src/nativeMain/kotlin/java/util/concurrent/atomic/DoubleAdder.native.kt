package java.util.concurrent.atomic

import java.io.Serializable

actual open class DoubleAdder actual constructor() : Number(), Serializable {
    private val baseBits = kotlin.concurrent.AtomicLong(0.0.toRawBits())

    actual open fun add(x: Double) {
        while (true) {
            val currentBits = baseBits.value
            val nextVal = Double.fromBits(currentBits) + x
            if (baseBits.compareAndSet(currentBits, nextVal.toRawBits())) {
                return
            }
        }
    }

    actual open fun sum(): Double = Double.fromBits(baseBits.value)

    actual open fun reset() {
        baseBits.value = 0.0.toRawBits()
    }

    actual open fun sumThenReset(): Double {
        while (true) {
            val currentBits = baseBits.value
            if (baseBits.compareAndSet(currentBits, 0.0.toRawBits())) {
                return Double.fromBits(currentBits)
            }
        }
    }

    override fun toString(): String = sum().toString()

    actual override fun toByte(): Byte = sum().toInt().toByte()
    actual override fun toShort(): Short = sum().toInt().toShort()
    actual override fun toInt(): Int = sum().toInt()
    actual override fun toLong(): Long = sum().toLong()
    actual override fun toFloat(): Float = sum().toFloat()
    actual override fun toDouble(): Double = sum()
}
