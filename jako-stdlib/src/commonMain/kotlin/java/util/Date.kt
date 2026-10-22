@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class Date : Serializable, Cloneable, Comparable<Date> {
    constructor()
    constructor(date: Long)

    open var time: Long
    open fun getTime(): Long
    open fun setTime(time: Long)
    override fun compareTo(other: Date): Int
    open fun before(whenDate: Date): Boolean
    open fun after(whenDate: Date): Boolean
    open fun toInstant(): java.time.Instant

    companion object {
        fun from(instant: java.time.Instant): Date
    }
}
