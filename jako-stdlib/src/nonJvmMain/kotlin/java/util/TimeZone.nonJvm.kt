package java.util

import java.io.Serializable
import java.lang.Cloneable
import java.time.ZoneId

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
    actual open fun getOffset(date: Long): Int {
        val spec = ZoneId.namedRuleSpec(zoneId)
        if (spec != null) {
            return spec.offsetAtInstant(date.floorDiv(1000L)).totalSeconds * 1000
        }
        return rawOffsetMillis
    }
    actual open fun getDisplayName(): String = zoneId
    actual open fun useDaylightTime(): Boolean {
        val spec = ZoneId.namedRuleSpec(zoneId)
        return spec?.usesDst ?: false
    }
    actual open fun inDaylightTime(date: Date): Boolean {
        val spec = ZoneId.namedRuleSpec(zoneId) ?: return false
        if (!spec.usesDst) return false
        return spec.offsetAtInstant(date.getTime().floorDiv(1000L)) == spec.dstOffset
    }

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
            val spec = ZoneId.namedRuleSpec(id)
            if (spec != null) {
                return TimeZone(id, spec.stdOffset.totalSeconds * 1000)
            }
            return TimeZone("GMT", 0)
        }

        actual fun getAvailableIDs(): Array<String> {
            return arrayOf(
                "UTC", "GMT", "Asia/Shanghai", "Asia/Tokyo", "Europe/London",
                "Europe/Paris", "Europe/Berlin", "America/New_York", "America/Chicago",
                "America/Denver", "America/Los_Angeles", "Australia/Sydney", "Pacific/Auckland"
            )
        }

        actual fun getAvailableIDs(rawOffset: Int): Array<String> {
            return getAvailableIDs().filter { getTimeZone(it).getRawOffset() == rawOffset }.toTypedArray()
        }
    }
}

