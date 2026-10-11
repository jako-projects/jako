package java.util

import java.io.Serializable
import java.lang.Cloneable

actual open class TimeZone : Serializable, Cloneable {
    private val zoneId: String
    private val rawOffsetMillis: Int

    constructor(id: String, rawOffsetMillis: Int) {
        this.zoneId = id
        this.rawOffsetMillis = rawOffsetMillis
    }

    constructor() : this("UTC", 0)

    actual open fun getID(): String = zoneId
    actual open fun getRawOffset(): Int = rawOffsetMillis
    actual open fun getOffset(date: Long): Int = rawOffsetMillis
    actual open fun getDisplayName(): String = zoneId
    actual open fun useDaylightTime(): Boolean = false
    actual open fun inDaylightTime(date: Date): Boolean = false

    actual open val id: String get() = getID()
    actual open val rawOffset: Int get() = getRawOffset()
    actual open val displayName: String get() = getDisplayName()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeZone) return false
        return zoneId == other.zoneId && rawOffsetMillis == other.rawOffsetMillis
    }

    actual override fun hashCode(): Int = zoneId.hashCode() xor rawOffsetMillis

    actual override fun toString(): String = "TimeZone[id=\"$zoneId\",offset=$rawOffsetMillis]"

    actual companion object {
        actual val SHORT: Int = 0
        actual val LONG: Int = 1

        private var defaultZone: TimeZone = TimeZone("UTC", 0)

        actual fun getDefault(): TimeZone = defaultZone

        actual fun setDefault(zone: TimeZone) {
            defaultZone = zone
        }

        actual fun getTimeZone(id: String): TimeZone {
            if (id.equals("UTC", ignoreCase = true)) {
                return TimeZone("UTC", 0)
            }
            if (id.equals("GMT", ignoreCase = true) || id.isEmpty()) {
                return TimeZone("GMT", 0)
            }
            if (id.startsWith("GMT+") || id.startsWith("GMT-") || id.startsWith("UTC+") || id.startsWith("UTC-")) {
                val sign = if (id[3] == '+') 1 else -1
                val rest = id.substring(4)
                val parts = rest.split(":")
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
                val offset = sign * ((hours * 3600 + minutes * 60) * 1000)
                return TimeZone(id, offset)
            }
            // Well-known offsets
            val offset = when (id) {
                "Asia/Shanghai", "Asia/Chongqing", "Asia/Hong_Kong", "PRC", "CTT" -> 8 * 3600 * 1000
                "Asia/Tokyo", "JST" -> 9 * 3600 * 1000
                "America/New_York", "EST" -> -5 * 3600 * 1000
                "America/Chicago", "CST" -> -6 * 3600 * 1000
                "America/Denver", "MST" -> -7 * 3600 * 1000
                "America/Los_Angeles", "PST" -> -8 * 3600 * 1000
                "Europe/London" -> 0
                "Europe/Paris", "Europe/Berlin", "CET" -> 1 * 3600 * 1000
                else -> 0
            }
            return TimeZone(id, offset)
        }

        actual fun getAvailableIDs(): Array<String> {
            return arrayOf(
                "UTC", "GMT", "Asia/Shanghai", "Asia/Tokyo", "Europe/London",
                "Europe/Paris", "America/New_York", "America/Chicago", "America/Los_Angeles"
            )
        }

        actual fun getAvailableIDs(rawOffset: Int): Array<String> {
            return getAvailableIDs().filter { getTimeZone(it).getRawOffset() == rawOffset }.toTypedArray()
        }
    }
}
