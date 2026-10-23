package java.util

import java.io.Serializable
import kotlin.random.Random

actual class UUID actual constructor(
    private val mostSigBits: Long,
    private val leastSigBits: Long
) : Serializable, Comparable<UUID> {

    actual fun getMostSignificantBits(): Long = mostSigBits
    actual fun getLeastSignificantBits(): Long = leastSigBits
    actual val mostSignificantBits: Long get() = getMostSignificantBits()
    actual val leastSignificantBits: Long get() = getLeastSignificantBits()

    fun version(): Int {
        return ((mostSigBits ushr 12) and 0x0F).toInt()
    }

    fun variant(): Int {
        return when ((leastSigBits ushr 61).toInt()) {
            in 0..3 -> 0
            in 4..5 -> 2
            6 -> 6
            7 -> 7
            else -> 0
        }
    }

    fun timestamp(): Long {
        if (version() != 1) throw UnsupportedOperationException("Not a version 1 UUID")
        return ((mostSigBits and 0xFFFL) shl 48) or
                (((mostSigBits ushr 16) and 0xFFFFL) shl 32) or
                (mostSigBits ushr 32)
    }

    fun clockSequence(): Int {
        if (version() != 1) throw UnsupportedOperationException("Not a version 1 UUID")
        return ((leastSigBits ushr 48) and 0x3FFF).toInt()
    }

    fun node(): Long {
        if (version() != 1) throw UnsupportedOperationException("Not a version 1 UUID")
        return leastSigBits and 0xFFFFFFFFFFFFL
    }

    actual override fun compareTo(other: UUID): Int {
        return if (this.mostSigBits != other.mostSigBits) {
            if (this.mostSigBits < other.mostSigBits) -1 else 1
        } else if (this.leastSigBits != other.leastSigBits) {
            if (this.leastSigBits < other.leastSigBits) -1 else 1
        } else {
            0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UUID) return false
        return mostSigBits == other.mostSigBits && leastSigBits == other.leastSigBits
    }

    override fun hashCode(): Int {
        val hilo = mostSigBits xor leastSigBits
        return (hilo.toInt()) xor (hilo ushr 32).toInt()
    }

    actual override fun toString(): String {
        return digits(mostSigBits ushr 32, 8) + "-" +
                digits(mostSigBits ushr 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits ushr 48, 4) + "-" +
                digits(leastSigBits, 12)
    }

    actual companion object {
        actual fun randomUUID(): UUID {
            val randomBytes = Random.nextBytes(16)
            randomBytes[6] = (randomBytes[6].toInt() and 0x0f or 0x40).toByte()
            randomBytes[8] = (randomBytes[8].toInt() and 0x3f or 0x80).toByte()
            var msb = 0L
            var lsb = 0L
            for (i in 0..7) msb = (msb shl 8) or (randomBytes[i].toLong() and 0xff)
            for (i in 8..15) lsb = (lsb shl 8) or (randomBytes[i].toLong() and 0xff)
            return UUID(msb, lsb)
        }

        actual fun nameUUIDFromBytes(name: ByteArray): UUID {
            var msb = 0L
            var lsb = 0L
            for (i in 0 until minOf(8, name.size)) msb = (msb shl 8) or (name[i].toLong() and 0xff)
            for (i in 8 until minOf(16, name.size)) lsb = (lsb shl 8) or (name[i].toLong() and 0xff)
            msb = (msb and 0xFFFFFFFFFFFF0FFFUL.toLong()) or 0x0000000000003000L
            lsb = (lsb and 0x3FFFFFFFFFFFFFFFUL.toLong()) or Long.MIN_VALUE
            return UUID(msb, lsb)
        }

        actual fun fromString(name: String): UUID {
            val components = name.split("-")
            if (components.size != 5) throw IllegalArgumentException("Invalid UUID string: $name")
            val msb1 = components[0].toLong(16)
            val msb2 = components[1].toLong(16)
            val msb3 = components[2].toLong(16)
            val lsb1 = components[3].toLong(16)
            val lsb2 = components[4].toLong(16)
            val msb = (msb1 shl 32) or (msb2 shl 16) or msb3
            val lsb = (lsb1 shl 48) or lsb2
            return UUID(msb, lsb)
        }

        private fun digits(val1: Long, digits: Int): String {
            val hi = 1L shl (digits * 4)
            return (hi or (val1 and (hi - 1))).toString(16).substring(1)
        }
    }
}
