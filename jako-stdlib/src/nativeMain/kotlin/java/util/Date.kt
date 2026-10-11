package java.util

import java.io.Serializable
import java.lang.Cloneable
import java.time.Instant

actual open class Date : Serializable, Cloneable, Comparable<Date> {
    actual open var time: Long = 0L

    actual constructor() : this(0L)

    actual constructor(date: Long) {
        this.time = date
    }

    actual open fun getTime(): Long = time
    actual open fun setTime(time: Long) {
        this.time = time
    }

    actual override fun compareTo(other: Date): Int = time.compareTo(other.time)
    actual open fun before(whenDate: Date): Boolean = time < whenDate.time
    actual open fun after(whenDate: Date): Boolean = time > whenDate.time

    actual open fun toInstant(): Instant = Instant.ofEpochMilli(time)

    override fun equals(other: Any?): Boolean = other is Date && time == other.time
    override fun hashCode(): Int = (time xor (time ushr 32)).toInt()
    override fun toString(): String = Date(time).toInstant().toString()

    actual companion object {
        actual fun from(instant: Instant): Date = Date(instant.toEpochMilli())
    }
}
