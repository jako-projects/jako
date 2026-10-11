@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class TimeZone : Serializable, Cloneable {
    open fun getID(): String
    open fun getRawOffset(): Int
    open fun getOffset(date: Long): Int
    open fun getDisplayName(): String
    open fun useDaylightTime(): Boolean
    open fun inDaylightTime(date: Date): Boolean

    open val id: String
    open val rawOffset: Int
    open val displayName: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        val SHORT: Int
        val LONG: Int

        fun getDefault(): TimeZone
        fun setDefault(zone: TimeZone)
        fun getTimeZone(id: String): TimeZone
        fun getAvailableIDs(): Array<String>
        fun getAvailableIDs(rawOffset: Int): Array<String>
    }
}
