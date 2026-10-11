@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time

import java.io.Serializable

expect abstract class ZoneId : Serializable {
    abstract val id: String
    abstract fun getId(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        // [Nullability Decision] zoneId is non-null, matching JDK 8 ZoneId.of(String)
        fun of(zoneId: String): ZoneId
        fun systemDefault(): ZoneId
        fun getAvailableZoneIds(): Set<String>
    }
}
